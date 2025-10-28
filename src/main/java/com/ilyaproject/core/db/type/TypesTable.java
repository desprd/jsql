package com.ilyaproject.core.db.type;

import java.util.Map;

public final class TypesTable {

    private TypesTable(){}

    public static final Map<JsqlType, Class<?>> convertTypes = Map.of(
            JsqlType.TEXT, String.class,
            JsqlType.BIGINT, Long.class,
            JsqlType.BOOLEAN, Boolean.class,
            JsqlType.CHARACTER, Character.class,
            JsqlType.INTEGER, Integer.class
    );
}
