package com.ilyaproject.core.db;

import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.dto.table.TableDto;
import com.ilyaproject.core.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableUtils {
    public static List<TableDto> getTablesByTablesNames(List<String> names, Database db) {
        if (names.isEmpty()) {
            throw new IllegalArgumentException("SELECT statement supposed to include at least one table");
        }
        Map<String, Table> allTables = db.getTables();
        List<TableDto> desiredTables = new ArrayList<>();
        for (String name: names) {
            Table tableToAdd = allTables.get(name);
            if (tableToAdd == null) {
                throw new IllegalArgumentException("Table with name " + name + " was not found in database");
            }
            desiredTables.add(tableToAdd.getTableData());
        }
        return desiredTables;
    }
}
