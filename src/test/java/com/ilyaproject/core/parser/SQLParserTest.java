package com.ilyaproject.core.parser;

import com.ilyaproject.core.dto.expression.ExpressionNode;
import com.ilyaproject.core.dto.expression.ExpressionUnit;
import com.ilyaproject.core.dto.expression.ExpressionUnitType;
import com.ilyaproject.core.dto.expression.SimpleExpression;
import com.ilyaproject.core.dto.query.SQLQuery;
import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.dto.token.Token;
import com.ilyaproject.core.dto.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLParserTest {

    private SQLParser parser;

    @BeforeEach
    void setUp() {
        parser = new SQLParser();
    }

    @Test
    void getLisOfSelectTokensWithWhereSimpleExpression_parsing_returnSelectQuery() throws Exception {
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
    void getLisOfSelectTokensWithWhereExpressionNode_parsing_returnSelectQuery() throws Exception {
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
    void getLisOfSelectTokensWithWhereExpressionNodeUnclosedParentheses_parsing_throwSQLException() throws Exception {
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
        SQLException e = assertThrows(
                SQLException.class,
                () -> parser.parseStatement(tokens)
        );
        assertEquals(
                "Failed to parse SELECT statement Statement with unclosed parentheses",
                e.getMessage()
        );
    }

}
