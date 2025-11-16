package com.ilyaproject.core.parser;

import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.expression.*;
import com.ilyaproject.core.dto.query.*;
import com.ilyaproject.core.dto.token.Token;
import com.ilyaproject.core.dto.token.TokenType;
import com.ilyaproject.core.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SQLParser {
    public SQLQuery parseStatement(List<Token> tokens){
        SQLQuery query = null;
        Token firstToken = tokens.removeFirst();
        if (firstToken.value().equals("SELECT") && firstToken.type() == TokenType.KEYWORD) {
            try {
                query = parseSelect(tokens);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Failed to parse SELECT statement " + e.getMessage());
            }
        }
        else if (firstToken.value().equals("INSERT") && firstToken.type() == TokenType.KEYWORD) {
            try {
                query = parseInsert(tokens);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Failed to parse INSERT statement " + e.getMessage());
            }
        }
        else if (firstToken.value().equals("CREATE") && firstToken.type() == TokenType.KEYWORD) {
            try {
                query = parseCreate(tokens);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Failed to parse CREATE statement " + e.getMessage());
            }
        }
        else if (firstToken.value().equals("DROP") && firstToken.type() == TokenType.KEYWORD) {
            parseDrop(tokens);
        }
        else {
            throw new IllegalArgumentException("Unexpected statement starts with " + firstToken.value());
        }
        return query;
    }

    private SelectQuery parseSelect(List<Token> tokens) {
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        parseColumnList(tokens, queryBuilder);
        if (tokenEquals(tokens, "FROM", TokenType.KEYWORD)) {
            tokens.removeFirst();
            parseFromClause(tokens, queryBuilder);
        } else {
            throw new IllegalArgumentException("Keyword FROM is necessary after SELECT");
        }
        if (tokenEquals(tokens, "WHERE", TokenType.KEYWORD)) {
            tokens.removeFirst();
            parseWhereClause(tokens, queryBuilder);
        }
        return queryBuilder.build();
    }

    private InsertIntoQuery parseInsert(List<Token> tokens) {
        if (!tokenEquals(tokens, "INTO", TokenType.KEYWORD)) {
            throw new IllegalArgumentException("Wrong format for INSERT statement");
        }
        tokens.removeFirst();
        InsertIntoQueryBuilder insertIntoQueryBuilder = new InsertIntoQueryBuilder();
        if (tokenEqualsByType(tokens, TokenType.IDENTIFIER)) {
            insertIntoQueryBuilder.addTableName(tokens.removeFirst().value());
        } else {
            throw new IllegalArgumentException("Table name was not found");
        }
        List<String> columns = new ArrayList<>();
        parseInsertData(tokens, columns);
        if (!tokenEquals(tokens, "VALUES", TokenType.KEYWORD)) {
            throw new IllegalArgumentException("Wrong format for INSERT statement");
        }
        tokens.removeFirst();
        List<String> values = new ArrayList<>();
        parseInsertData(tokens, values);
        if (columns.size() != values.size()) {
            throw new IllegalArgumentException(
                    "Number of line to insert doesn't match with numbers with given columns"
            );
        }
        for (int i = 0; i < values.size(); i++) {
            insertIntoQueryBuilder.addValuesToColumns(columns.get(i), values.get(i));
        }
        return insertIntoQueryBuilder.build();
    }

    private CreateTableQuery parseCreate(List<Token> tokens) {
        if (!tokenEquals(tokens, "TABLE", TokenType.KEYWORD)) {
            throw new IllegalArgumentException("Wrong format for CREATE statement");
        }
        tokens.removeFirst();
        CreateTableBuilder createTableBuilder = new CreateTableBuilder();
        if (tokenEqualsByType(tokens, TokenType.IDENTIFIER)) {
            createTableBuilder.addTableName(tokens.removeFirst().value());
        } else {
            throw new IllegalArgumentException("Table name was not found");
        }
        parseColumnsToCreate(tokens, createTableBuilder);
        return createTableBuilder.build();
    }

    private void parseDrop(List<Token> tokens) {
        // TODO
    }

    private void parseColumnList(List<Token> tokens, SelectQueryBuilder queryBuilder) {
        if (tokenEquals(tokens, "(", TokenType.SYMBOL)) {
            tokens.removeFirst();
        }
        if (tokenEquals(tokens, "*", TokenType.SYMBOL)) {
            tokens.removeFirst();
        } else if (tokenEqualsByType(tokens, TokenType.IDENTIFIER)) {
            queryBuilder.addColumn(tokens.removeFirst().value());
            if (tokenEquals(tokens, ",", TokenType.SYMBOL)) {
                tokens.removeFirst();
                parseColumnList(tokens, queryBuilder);
            }
        } else {
            throw new IllegalArgumentException("Unexpected value after SELECT - " + tokens.getFirst().value());
        }
        if (tokenEquals(tokens, ")", TokenType.SYMBOL)) {
            tokens.removeFirst();
        }
    }

    private void parseFromClause(List<Token> tokens, SelectQueryBuilder queryBuilder) {
        if (tokenEquals(tokens, "(", TokenType.SYMBOL)) {
            tokens.removeFirst();
        }
        if (tokenEqualsByType(tokens, TokenType.IDENTIFIER)) {
            queryBuilder.addTable(tokens.removeFirst().value());
            if (tokenEquals(tokens, ",", TokenType.SYMBOL)) {
                tokens.removeFirst();
                parseFromClause(tokens, queryBuilder);
            }
        } else {
            throw new IllegalArgumentException("Unexpected value after SELECT " + tokens.getFirst().value());
        }
        if (tokenEquals(tokens, ")", TokenType.SYMBOL)) {
            tokens.removeFirst();
        }

    }

    private void parseWhereClause(List<Token> tokens, SelectQueryBuilder queryBuilder){
        Expression expression = parseExpression(tokens);
        queryBuilder.addConditions(expression);
    }

    private Expression parseExpression(List<Token> tokens){
        Expression left = parseTerm(tokens);
        while (
                tokenEquals(tokens, "AND", TokenType.KEYWORD) ||
                        tokenEquals(tokens, "AND", TokenType.KEYWORD)
        ) {
            String operator = tokens.removeFirst().value();
            Expression right = parseTerm(tokens);
            left = new ExpressionNode(operator, left, right);
        }
        return left;
    }

    private Expression parseTerm(List<Token> tokens){
        if (tokenEquals(tokens, "(", TokenType.SYMBOL)) {
            tokens.removeFirst();
            Expression expression = parseExpression(tokens);
            if (tokenEquals(tokens, ")", TokenType.SYMBOL)) {
                tokens.removeFirst();
            } else {
                throw new IllegalArgumentException("Statement with unclosed parentheses");
            }
            return expression;
        } else {
            return parseSimpleExpression(tokens);
        }
    }

    private SimpleExpression parseSimpleExpression(List<Token> tokens) {
        List<ExpressionUnit> units = new ArrayList<>();
        while (!tokens.isEmpty()) {
            if (tokenEqualsByType(tokens, TokenType.IDENTIFIER) ||
                tokenEqualsByType(tokens, TokenType.TEXT)) {
                ExpressionUnit unit = new ExpressionUnit(
                        tokens.removeFirst().value(),
                        ExpressionUnitType.TEXT
                );
                units.add(unit);
            } else if (tokenEqualsByType(tokens, TokenType.NUMBER)) {
                ExpressionUnit unit = new ExpressionUnit(
                        tokens.removeFirst().value(),
                        ExpressionUnitType.NUMERIC
                );
                units.add(unit);
            } else if (isValidSymbol(tokens)) {
                ExpressionUnit unit = new ExpressionUnit(
                        tokens.removeFirst().value(),
                        ExpressionUnitType.SYMBOL
                );
                units.add(unit);
            } else {
                break;
            }
        }
        return new SimpleExpression(units);
    }

    private void parseColumnsToCreate(List<Token> tokens, CreateTableBuilder createTableBuilder) {
        if (!tokenEquals(tokens, "(", TokenType.SYMBOL)){
            throw new IllegalArgumentException("Parenthesis were expected");
        }
        tokens.removeFirst();
        parseColumn(tokens, createTableBuilder);
        if (!tokenEquals(tokens, ")", TokenType.SYMBOL)){
            throw new IllegalArgumentException("Parenthesis were not closed");
        }
        tokens.removeFirst();
    }

    private void parseColumn(List<Token> tokens, CreateTableBuilder createTableBuilder) {
        if (tokenEqualsByType(tokens, TokenType.IDENTIFIER)) {
            String fieldName = tokens.removeFirst().value();
            if (isValidJsqlType(tokens)) {
                JsqlType fieldType = JsqlType.valueOf(tokens.removeFirst().value().toUpperCase());
                createTableBuilder.addField(fieldName, fieldType);
            } else {
                throw new IllegalArgumentException("Field type was not found");
            }
            if (tokenEquals(tokens, ",", TokenType.SYMBOL)) {
                tokens.removeFirst();
                parseColumn(tokens, createTableBuilder);
            }
        } else {
            throw new IllegalArgumentException("Field name was not found");
        }
    }

    private void parseInsertData(List<Token> tokens, List<String> columns) {
        if (!tokenEquals(tokens, "(", TokenType.SYMBOL)){
            throw new IllegalArgumentException("Parenthesis were expected");
        }
        tokens.removeFirst();
        parseInsertLine(tokens, columns);
        if (!tokenEquals(tokens, ")", TokenType.SYMBOL)){
            throw new IllegalArgumentException("Parenthesis were not closed");
        }
        tokens.removeFirst();
    }

    private void parseInsertLine(List<Token> tokens, List<String> columns) {
        if (tokenEqualsByType(tokens, TokenType.IDENTIFIER)) {
            columns.add(tokens.removeFirst().value());
            if (tokenEquals(tokens, ",", TokenType.SYMBOL)) {
                tokens.removeFirst();
                parseInsertLine(tokens, columns);
            }
        } else {
            throw new IllegalArgumentException("Field name was not found");
        }
    }

    private boolean tokenEquals(List<Token> tokens, String value, TokenType type) {
        return !tokens.isEmpty() &&
                tokens.getFirst().value().equals(value) &&
                tokens.getFirst().type() == type;
    }

    private boolean tokenEqualsByType(List<Token> tokens, TokenType type) {
        return !tokens.isEmpty() &&
                tokens.getFirst().type() == type;
    }

    private boolean isValidSymbol(List<Token> tokens) {
        return !tokens.isEmpty() &&
                Constants.VALID_EXPRESSION_SYMBOLS.contains(tokens.getFirst().value()) &&
                tokens.getFirst().type() == TokenType.SYMBOL;
    }

    private boolean isValidJsqlType(List<Token> tokens) {
        return !tokens.isEmpty() &&
                tokens.getFirst().type() == TokenType.IDENTIFIER &&
                Constants.JSQL_TYPES.contains(tokens.getFirst().value().toUpperCase());
    }

}
