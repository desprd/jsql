package com.ilyaproject.core.utils;

import com.ilyaproject.core.db.type.JsqlType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class Constants {

    private Constants() {}

    public static final Set<Character> SPECIAL_CHARACTERS = Set.of(
            '(', ')', ',', ';', '=', '+', '-', '*', '/', '<', '>', '!', '%', '.'
    );

    public static final Set<String> VALID_EXPRESSION_SYMBOLS = Set.of(
            ">", "<", "/", "*", "-", "+", "="
    );

    public static final Set<String> JSQL_TYPES =
            Arrays.stream(JsqlType.values())
                    .map(Enum::name)
                    .collect(Collectors.toUnmodifiableSet());
}
