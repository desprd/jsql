package com.ilyaproject.parser;

import com.ilyaproject.core.dto.Token;
import com.ilyaproject.core.dto.TokenType;
import com.ilyaproject.core.parser.SQLTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SQLTokenizerTest {

    @Test
    void getRowSqlString_tokenized_returnListOfCorrectTokens() {
        // Given
        SQLTokenizer tokenizer = new SQLTokenizer();
        List<Token> expected = List.of(
                new Token(TokenType.KEYWORD, "INSERT"),
                new Token(TokenType.KEYWORD, "INTO"),
                new Token(TokenType.IDENTIFIER, "users"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.IDENTIFIER, "id"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.IDENTIFIER, "name"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.KEYWORD, "VALUES"),
                new Token(TokenType.SYMBOL, "("),
                new Token(TokenType.NUMBER, "1"),
                new Token(TokenType.SYMBOL, ","),
                new Token(TokenType.TEXT, "Alice"),
                new Token(TokenType.SYMBOL, ")"),
                new Token(TokenType.SYMBOL, ";")
        );

        // When
        List<Token> tokens = tokenizer.tokenize("INSERT INTO users (id, name) VALUES (1, 'Alice');");

        // Then
        assertEquals(expected, tokens);
    }

    @Test
    void getRowSqlStringWithNotValidCharacters_tokenized_returnLisOfTokensOfUnknownType() {
        // Given
        SQLTokenizer tokenizer = new SQLTokenizer();

        // When
        List<Token> tokens = tokenizer.tokenize("?@$");

        // Then
        assertEquals(TokenType.UNKNOWN, tokens.get(0).type());
        assertEquals("?", tokens.get(0).value());
        assertEquals(TokenType.UNKNOWN, tokens.get(1).type());
        assertEquals("@", tokens.get(1).value());
        assertEquals(TokenType.UNKNOWN, tokens.get(2).type());
        assertEquals("$", tokens.get(2).value());
    }
}
