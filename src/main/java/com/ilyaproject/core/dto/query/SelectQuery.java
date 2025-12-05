package com.ilyaproject.core.dto.query;

import com.ilyaproject.core.dto.expression.Expression;

import java.util.ArrayList;
import java.util.List;
public record SelectQuery(
        List<String> tables,
        List<String> columns,
        Expression conditions,
        Integer limit
) implements SQLQuery {
    public SelectQuery {
        tables = tables == null ? new ArrayList<>() : new ArrayList<>(tables);
        columns = columns == null ? new ArrayList<>() : new ArrayList<>(columns);
    }
}