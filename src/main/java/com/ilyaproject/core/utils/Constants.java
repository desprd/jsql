package com.ilyaproject.core.utils;

import java.util.HashSet;
import java.util.Set;

public final class Constants {

    private Constants() {}

    public static final Set<Character> SPECIAL_CHARACTERS = Set.of(
            '(', ')', ',', ';', '=', '+', '-', '*', '/', '<', '>', '!', '%', '.'
    );

    public static final Set<String> VALID_EXPRESSION_SYMBOLS = Set.of(
            ">", "<", "/", "*", "-", "+", "="
    );
}
