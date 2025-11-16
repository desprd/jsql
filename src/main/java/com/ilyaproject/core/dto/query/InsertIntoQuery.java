package com.ilyaproject.core.dto.query;

import java.util.HashMap;
import java.util.Map;

public record InsertIntoQuery(
        String tableName,
        Map<String, String> columnsToValues
) implements SQLQuery {
    public InsertIntoQuery {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("Table name cannot be empty");
        }
        columnsToValues = columnsToValues == null ? new HashMap<>() : new HashMap<>(columnsToValues);
    }
}
