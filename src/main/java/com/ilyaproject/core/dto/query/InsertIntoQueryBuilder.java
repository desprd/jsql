package com.ilyaproject.core.dto.query;

import java.util.HashMap;
import java.util.Map;

public class InsertIntoQueryBuilder {
    private String tableName;
    private final Map<String, String> valuesToColumns = new HashMap<>();

    public InsertIntoQueryBuilder addTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public InsertIntoQueryBuilder addValuesToColumns(String column, String value) {
        valuesToColumns.put(column, value);
        return this;
    }

    public InsertIntoQuery build() {
        return new InsertIntoQuery(tableName, valuesToColumns);
    }
}
