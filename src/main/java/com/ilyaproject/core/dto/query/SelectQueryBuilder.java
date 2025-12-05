package com.ilyaproject.core.dto.query;

import com.ilyaproject.core.dto.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public class SelectQueryBuilder {
    private final List<String> tables = new ArrayList<>();
    private final List<String> columns = new ArrayList<>();
    private Expression conditions;
    private Integer limit = null;

    public SelectQueryBuilder addTable(String table) {
        tables.add(table);
        return this;
    }

    public SelectQueryBuilder addColumn(String column) {
        columns.add(column);
        return this;
    }

    public SelectQueryBuilder addConditions(Expression conditions) {
        this.conditions = conditions;
        return this;
    }

    public SelectQueryBuilder addLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public SelectQuery build() {
        return new SelectQuery(tables, columns, conditions, limit);
    }

}