package com.ilyaproject.core.dto.query;

import com.ilyaproject.core.dto.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public record SelectQuery(
        List<String> tables,
        List<String> columns,
        Expression conditions
) implements SQLQuery {
    public SelectQuery() {
        this(
                new ArrayList<>(),
                new ArrayList<>(),
                null
        );
    }
}