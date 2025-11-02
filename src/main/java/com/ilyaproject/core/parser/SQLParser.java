package com.ilyaproject.core.parser;

import com.ilyaproject.core.dto.expression.*;
import com.ilyaproject.core.dto.query.SQLQuery;
import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.dto.query.SelectQueryBuilder;
import com.ilyaproject.core.dto.token.Token;
import com.ilyaproject.core.dto.token.TokenType;
import com.ilyaproject.core.utils.Constants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLParser {

    public SQLQuery parseStatement(List<Token> tokens) throws SQLException {
        SQLQuery query = null;
        Token firstToken = tokens.removeFirst();
        if (firstToken.value().equals("SELECT") && firstToken.type() == TokenType.KEYWORD) {
            try {
                query = parseSelect(tokens);
            } catch (SQLException e) {
                throw new SQLException("Failed to parse SELECT statement " + e.getMessage());
            }
        }
        else if (firstToken.value().equals("INSERT") && firstToken.type() == TokenType.KEYWORD) {
            parseInsert(tokens);
        }
        else if (firstToken.value().equals("CREATE") && firstToken.type() == TokenType.KEYWORD) {
            parseCreate(tokens);
        }
        else if (firstToken.value().equals("DROP") && firstToken.type() == TokenType.KEYWORD) {
            parseDrop(tokens);
        }
        else {
            throw new SQLException("Unexpected statement starts with " + firstToken.value());
        }
        return query;
    }

    private SelectQuery parseSelect(List<Token> tokens) throws SQLException{
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        parseColumnList(tokens, queryBuilder);
        if (tokenEquals(tokens, "FROM", TokenType.KEYWORD)) {
            tokens.removeFirst();
            parseFromClause(tokens, queryBuilder);
        } else {
            throw new SQLException("Keyword FROM is necessary after SELECT");
        }
        if (tokenEquals(tokens, "WHERE", TokenType.KEYWORD)) {
            tokens.removeFirst();
            parseWhereClause(tokens, queryBuilder);
        }
        return queryBuilder.build();
    }

    private void parseInsert(List<Token> tokens) {
        // TODO
    }

    private void parseCreate(List<Token> tokens) {
        // TODO
    }

    private void parseDrop(List<Token> tokens) {
        // TODO
    }

    private void parseColumnList(List<Token> tokens, SelectQueryBuilder queryBuilder) throws SQLException{
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
            throw new SQLException("Unexpected value after SELECT - " + tokens.getFirst().value());
        }
        if (tokenEquals(tokens, ")", TokenType.SYMBOL)) {
            tokens.removeFirst();
        }
    }

    private void parseFromClause(List<Token> tokens, SelectQueryBuilder queryBuilder) throws SQLException {
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
            throw new SQLException("Unexpected value after SELECT " + tokens.getFirst().value());
        }
        if (tokenEquals(tokens, ")", TokenType.SYMBOL)) {
            tokens.removeFirst();
        }

    }

    private void parseWhereClause(List<Token> tokens, SelectQueryBuilder queryBuilder) throws SQLException{
        Expression expression = parseExpression(tokens);
        queryBuilder.addConditions(expression);
    }

    private Expression parseExpression(List<Token> tokens) throws SQLException{
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

    private Expression parseTerm(List<Token> tokens) throws SQLException{
        if (tokenEquals(tokens, "(", TokenType.SYMBOL)) {
            tokens.removeFirst();
            Expression expression = parseExpression(tokens);
            if (tokenEquals(tokens, ")", TokenType.SYMBOL)) {
                tokens.removeFirst();
            } else {
                throw new SQLException("Statement with unclosed parentheses");
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

}
