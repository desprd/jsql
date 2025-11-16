package com.ilyaproject.core.parser;

import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.expression.ExpressionNode;
import com.ilyaproject.core.dto.expression.ExpressionUnit;
import com.ilyaproject.core.dto.expression.ExpressionUnitType;
import com.ilyaproject.core.dto.expression.SimpleExpression;
import com.ilyaproject.core.dto.query.CreateTableQuery;
import com.ilyaproject.core.dto.query.InsertIntoQuery;
import com.ilyaproject.core.dto.query.SQLQuery;
import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.dto.token.Token;
import com.ilyaproject.core.dto.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SQLParserTest {

    private SQLParser parser;

    @BeforeEach
    void setUp() {
        parser = new SQLParser();
    }

    @Test
    void getListOfSelectTokensAllColumnsNoExpression_parsing_returnSelectQuery() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "SELECT"),
                new Token(TokenType.SYMBOL, "*"),
                new Token(TokenType.KEYWORD, "FROM"),
                new Token(TokenType.IDENTIFIER, "countries")

        ));

        // When
        SQLQuery query = parser.parseStatement(tokens);

        // Then
        assertInstanceOf(SelectQuery.class, query);
        SelectQuery selectQuery = (SelectQuery) query;
        assertEquals(List.of("countries"), selectQuery.tables());
        assertTrue(selectQuery.columns().isEmpty());
        assertNull(selectQuery.conditions());
    }

    @Test
    void getLisOfSelectTokensWithWhereSimpleExpression_parsing_returnSelectQuery() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "SELECT"),
                new Token(TokenType.IDENTIFIER, "country_id"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.KEYWORD, "FROM"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.KEYWORD, "WHERE"),
                new Token(TokenType.IDENTIFIER, "region_id"),
                new Token(TokenType.SYMBOL, "="),
                new Token(TokenType.NUMBER, "1")

        ));
        List<String> expectedTableList = new ArrayList<>(List.of(
                "countries"
        ));
        List<String> expectedColumns = new ArrayList<>(List.of(
                "country_id",
                "country_name"
        ));
        List<ExpressionUnit> units = new ArrayList<>(List.of(
                new ExpressionUnit("region_id", ExpressionUnitType.TEXT),
                new ExpressionUnit("=", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("1", ExpressionUnitType.NUMERIC)
        ));
        SimpleExpression expectedExpression = new SimpleExpression(units);

        // When
        SQLQuery query = parser.parseStatement(tokens);

        // Then
        assertInstanceOf(SelectQuery.class, query);
        SelectQuery selectQuery = (SelectQuery) query;
        assertEquals(expectedTableList, selectQuery.tables());
        assertEquals(expectedColumns, selectQuery.columns());
        assertEquals(expectedExpression, selectQuery.conditions());
    }

    @Test
    void getLisOfSelectTokensWithWhereExpressionNode_parsing_returnSelectQuery() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "SELECT"),
                new Token(TokenType.IDENTIFIER, "country_id"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.KEYWORD, "FROM"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.KEYWORD, "WHERE"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "region_id"),
                new Token(TokenType.SYMBOL, "="),
                new Token(TokenType.NUMBER, "1"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "AND"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ">"),
                new Token(TokenType.NUMBER, "30000"),
                new Token(TokenType.SYMBOL, ")")
        ));
        List<ExpressionUnit> leftUnits = new ArrayList<>(List.of(
                new ExpressionUnit("region_id", ExpressionUnitType.TEXT),
                new ExpressionUnit("=", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("1", ExpressionUnitType.NUMERIC)
        ));
        SimpleExpression left = new SimpleExpression(leftUnits);
        List<ExpressionUnit> rightUnits = new ArrayList<>(List.of(
                new ExpressionUnit("area", ExpressionUnitType.TEXT),
                new ExpressionUnit(">", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("30000", ExpressionUnitType.NUMERIC)
        ));
        SimpleExpression right = new SimpleExpression(rightUnits);
        ExpressionNode expectedExpression = new ExpressionNode("AND", left, right);

        // When
        SelectQuery query = (SelectQuery) parser.parseStatement(tokens);

        // Then
        assertEquals(expectedExpression, query.conditions());
    }

    @Test
    void getListOfSelectTokensWithWhereExpressionNodeUnclosedParentheses_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "SELECT"),
                new Token(TokenType.IDENTIFIER, "country_id"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.KEYWORD, "FROM"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.KEYWORD, "WHERE"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "region_id"),
                new Token(TokenType.SYMBOL, "="),
                new Token(TokenType.NUMBER, "1"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "AND"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ">"),
                new Token(TokenType.NUMBER, "30000")
        ));

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );
        assertEquals(
                "Failed to parse SELECT statement Statement with unclosed parentheses",
                e.getMessage()
        );
    }

    @Test
    void getListOfCreateTableTokens_parsing_returnCreateTableQuery() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "CREATE"),
                new Token(TokenType.KEYWORD, "TABLE"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.IDENTIFIER, "TEXT"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.IDENTIFIER, "BIGINT"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.IDENTIFIER, "BIGINT"),
                new Token(TokenType.SYMBOL, ")")

        ));
        Map<String, JsqlType> expectedFields = Map.of(
                "country_name", JsqlType.TEXT,
                "area", JsqlType.BIGINT,
                "population", JsqlType.BIGINT
        );

        // When
        CreateTableQuery query = (CreateTableQuery) parser.parseStatement(tokens);

        // Then
        assertEquals("countries", query.tableName());
        assertEquals(expectedFields, query.fields());
    }

    @Test
    void getListOfCreateTableTokensNoTableName_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "CREATE"),
                new Token(TokenType.KEYWORD, "TABLE"),
                new Token(TokenType.SYMBOL, "(")
        ));

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );
        assertEquals("Failed to parse CREATE statement Table name was not found", e.getMessage());
    }

    @Test
    void getListOfCreateTableTokensNoOpenParentheses_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "CREATE"),
                new Token(TokenType.KEYWORD, "TABLE"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.IDENTIFIER, "country_name")
        ));

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );
        assertEquals("Failed to parse CREATE statement Parenthesis were expected", e.getMessage());
    }

    @Test
    void getListOfCreateTableTokensNoFieldName_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "CREATE"),
                new Token(TokenType.KEYWORD, "TABLE"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "TEXT")
        ));

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );
        assertEquals("Failed to parse CREATE statement Field name was not found", e.getMessage());
    }

    @Test
    void getListOfCreateTableTokensNoFieldType_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "CREATE"),
                new Token(TokenType.KEYWORD, "TABLE"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, "(")
        ));

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );
        assertEquals("Failed to parse CREATE statement Field type was not found", e.getMessage());
    }

    @Test
    void getListOfCreateTableTokensNoCloseParentheses_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "CREATE"),
                new Token(TokenType.KEYWORD, "TABLE"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.IDENTIFIER, "TEXT"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.IDENTIFIER, "BIGINT"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.IDENTIFIER, "BIGINT")
        ));

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );
        assertEquals("Failed to parse CREATE statement Parenthesis were not closed", e.getMessage());
    }

    @Test
    void getListOfInsertIntoTokens_parsing_returnInsertIntoQuery() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));
        HashMap<String, String> rowsValues = new HashMap<>(
                Map.of(
                        "country_name", "USA",
                        "area", "9867000",
                        "population", "348000000"
                )
        );
        InsertIntoQuery expectedQuery = new InsertIntoQuery("countries", rowsValues);

        // When
        SQLQuery resultQuery = parser.parseStatement(tokens);

        //Then
        assertInstanceOf(InsertIntoQuery.class, resultQuery);
        assertEquals(expectedQuery, resultQuery);
    }

    @Test
    void getListOfInsertIntoTokensMoreValuesThanColumns_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "something"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));

        // When
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );

        // Then
        assertEquals(
                "Failed to parse INSERT statement Number of line to insert doesn't match" +
                        " with numbers with given columns",
                e.getMessage()
        );
    }

    @Test
    void getListOfInsertIntoTokensNoIntoKeyWord_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));

        // When
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );

        // Then
        assertEquals(
                "Failed to parse INSERT statement Wrong format for INSERT statement",
                e.getMessage()
        );
    }

    @Test
    void getListOfInsertIntoTokensNoTableName_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));

        // When
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );

        // Then
        assertEquals(
                "Failed to parse INSERT statement Table name was not found",
                e.getMessage()
        );
    }

    @Test
    void getListOfInsertIntoTokensNoValuesKeyWord_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));

        // When
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );

        // Then
        assertEquals(
                "Failed to parse INSERT statement Wrong format for INSERT statement",
                e.getMessage()
        );
    }

    @Test
    void getListOfInsertIntoTokensNoOpenParentheses_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));

        // When
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );

        // Then
        assertEquals(
                "Failed to parse INSERT statement Parenthesis were expected",
                e.getMessage()
        );
    }

    @Test
    void getListOfInsertIntoTokensNoClosingParentheses_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "population"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));

        // When
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );

        // Then
        assertEquals(
                "Failed to parse INSERT statement Parenthesis were not closed",
                e.getMessage()
        );
    }

    @Test
    void getListOfInsertIntoTokensInaccurateDataSequence_parsing_throwIllegalArgumentException() {
        // Given
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.IDENTIFIER, "countries"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "country_name"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "area"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "USA"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "9867000"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "348000000"),
                new Token(TokenType.SYMBOL, ")")
        ));

        // When
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseStatement(tokens)
        );

        // Then
        assertEquals(
                "Failed to parse INSERT statement Field name was not found",
                e.getMessage()
        );
    }

}
