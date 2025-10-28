package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.dto.table.TableDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SelectExecutor implements StatementExecutor<SelectQuery>{
    @Override
    public SQLResponse get(SelectQuery query, Database db) {
        List<TableDto> tables = TableUtils.getTablesByTablesNames(query.tables(), db);
        extractRequiredColumns(tables, query.columns());
        return null;
    }

    private void extractRequiredColumns(List<TableDto> tables, List<String> columns) {
        if (columns.isEmpty()) {
            return;
        }

        for (TableDto table : tables) {
            table.schema().keySet().removeIf(key -> !columns.contains(key));
            for (Map<String, Object> row : table.rows()) {
                row.keySet().removeIf(key -> !columns.contains(key));
            }
        }
    }
}
