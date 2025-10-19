package com.ilyaproject.core.model.type;

import java.util.HashMap;
import java.util.Map;

public class TypesTable {
    public static final Map<JsqlType, Class<?>> convertTypes = new HashMap<>();
    static {
        convertTypes.put(JsqlType.TEXT, String.class);
        convertTypes.put(JsqlType.BIGINT, Long.class);
        convertTypes.put(JsqlType.BOOLEAN, Boolean.class);
        convertTypes.put(JsqlType.CHARACTER, Character.class);
        convertTypes.put(JsqlType.INTEGER, Integer.class);
    }
}
