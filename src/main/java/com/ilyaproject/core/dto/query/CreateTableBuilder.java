package com.ilyaproject.core.dto.query;

import com.ilyaproject.core.db.type.JsqlType;

import java.util.HashMap;
import java.util.Map;

public class CreateTableBuilder {
    private String tableName;
    private final Map<String, JsqlType> fields = new HashMap<>();

    public CreateTableBuilder addTableName(String name) {
        tableName = name;
        return this;
    }

    public CreateTableBuilder addField(String title, JsqlType type) {
        fields.put(title, type);
        return this;
    }

    public CreateTableQuery build() {
        return new CreateTableQuery(tableName, fields);
    }
}
