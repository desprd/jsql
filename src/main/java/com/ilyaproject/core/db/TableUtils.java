package com.ilyaproject.core.db;

import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.query.CreateTableQuery;
import com.ilyaproject.core.dto.table.TableDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableUtils {

    private TableUtils(){};

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

    public static void createTable(CreateTableQuery query, Database db) {
        Map<String, JsqlType> schema = query.fields();
        if (schema.isEmpty()) {
            throw new IllegalArgumentException("CREATE TABLE statement supposed to have schema");
        }
        String name = query.tableName();
        if (name.isBlank()) {
            throw new IllegalArgumentException("CREATE TABLE statement supposed to have name");
        }
        Map<String, Table> allTables = db.getTables();
        if (allTables.containsKey(name)) {
            throw new IllegalArgumentException("Table " + name + " already exists");
        }
        db.createTable(name, schema);
    }
}
