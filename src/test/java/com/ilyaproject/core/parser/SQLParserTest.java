package com.ilyaproject.core.parser;

import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.dto.token.Token;
import com.ilyaproject.core.dto.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class SQLParserTest {

    private List<Token> tokens;

    @BeforeEach
    void setUp() {
        tokens = new ArrayList<>(List.of(
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
    }

    @Test
    void testSelect() throws Exception {

    }

}
