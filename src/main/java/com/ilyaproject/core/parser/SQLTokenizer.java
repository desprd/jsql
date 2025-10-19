package com.ilyaproject.core.parser;

import com.ilyaproject.core.dto.Keyword;
import com.ilyaproject.core.dto.Token;
import com.ilyaproject.core.dto.TokenType;
import com.ilyaproject.core.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public final class SQLTokenizer {
    public List<Token> tokenize(String rowSql) {
        List<Token> tokens = new ArrayList<>();
        char[] rowSqlCharacters = rowSql.toCharArray();
        int pointer = 0;
        StringBuilder current = new StringBuilder();
        while (pointer < rowSqlCharacters.length) {
            if (
                    Character.isLetter(rowSqlCharacters[pointer]) || rowSqlCharacters[pointer] == '_'
            ) {
                while (
                        pointer < rowSqlCharacters.length &&
                        (Character.isLetter(rowSqlCharacters[pointer]) || rowSqlCharacters[pointer] == '_')
                ) {
                    current.append(rowSqlCharacters[pointer]);
                    pointer++;
                }
                tokens.add(
                        getKeywordOrIdentifierToken(current.toString())
                );
                current.delete(0, current.length());
            }
            else if (
                    Character.isDigit(rowSqlCharacters[pointer])
            ) {
                while (
                        pointer < rowSqlCharacters.length &&
                        Character.isDigit(rowSqlCharacters[pointer])
                ) {
                    current.append(rowSqlCharacters[pointer]);
                    pointer++;
                }
                tokens.add(
                        new Token(
                                TokenType.NUMBER,
                                current.toString()
                        )
                );
                current.delete(0, current.length());
            }
            else if (
                    Constants.SPECIAL_CHARACTERS.contains(rowSqlCharacters[pointer])
            ) {
                while (
                        pointer < rowSqlCharacters.length &&
                        Constants.SPECIAL_CHARACTERS.contains(rowSqlCharacters[pointer])
                ) {
                    current.append(rowSqlCharacters[pointer]);
                    pointer++;
                }
                tokens.add(
                        new Token(
                                TokenType.SYMBOL,
                                current.toString()
                        )
                );
                current.delete(0, current.length());
            }
            else if (
                    rowSqlCharacters[pointer] == '\'' || rowSqlCharacters[pointer] == '"'
            ) {
                pointer++;
                while (
                        pointer < rowSqlCharacters.length &&
                        (rowSqlCharacters[pointer] != '\'' || rowSqlCharacters[pointer] != '"')
                ) {
                    current.append(rowSqlCharacters[pointer]);
                    pointer++;
                }
                tokens.add(
                        new Token(
                                TokenType.TEXT,
                                current.toString()
                        )
                );
                current.delete(0, current.length());
            }
            else {
                pointer++;
            }
        }
        return tokens;
    }

    private Token getKeywordOrIdentifierToken(String str) {
        try {
            Keyword keyword = Keyword.valueOf(str.toUpperCase());
            return new Token(TokenType.KEYWORD, keyword.name());
        } catch (IllegalArgumentException ex) {
            return new Token(TokenType.IDENTIFIER, str);
        }
    }

}
