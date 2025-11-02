package com.ilyaproject.core.dto.query;

import com.ilyaproject.core.db.type.JsqlType;

import java.util.HashMap;
import java.util.Map;

public record CreateTableQuery(
        String tableName,
        Map<String, JsqlType> fields
) implements SQLQuery {
    public CreateTableQuery {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("Table name cannot be empty");
        }
        fields = fields == null ? new HashMap<>(): new HashMap<>(fields);
    }
}
