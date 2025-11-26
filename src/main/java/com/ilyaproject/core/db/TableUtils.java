package com.ilyaproject.core.db;

import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.query.CreateTableQuery;
import com.ilyaproject.core.dto.query.InsertIntoQuery;
import com.ilyaproject.core.dto.table.TableDto;
import com.ilyaproject.core.utils.DataUtils;

import java.util.*;

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

    public static void insertIntoTable(InsertIntoQuery query, Database db) {
        Map<String, Object> dataToInsert = new HashMap<>();
        Optional<Table> fetchedTable = db.getTableByName(query.tableName());
        if (fetchedTable.isEmpty()) {
            throw new IllegalArgumentException("Table " + query.tableName() + " was not found in database");
        }
        Table tableToInsert = fetchedTable.get();
        Map<String, JsqlType> schema = tableToInsert.getTableData().schema();
        for (String columnName: query.columnsToValues().keySet()) {
            if (!schema.containsKey(columnName)) {
                throw new IllegalArgumentException(
                        query.tableName() +
                        " table's schema doesn't contains column " +
                        columnName
                );
            }
            dataToInsert.put(columnName, parseToCorrectObject(query.columnsToValues().get(columnName), schema.get(columnName)));
        }
        db.insert(query.tableName(), dataToInsert);
    }

    public static void removeTableByName(String tableName, Database db) {
        Table table = db.removeTableByName(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Database doesn't contain table " + tableName);
        }
    }

    private static Object parseToCorrectObject(String value, JsqlType type) {
        try {
            return switch (type) {
                case INTEGER -> Integer.parseInt(value);
                case BOOLEAN -> {
                    if (value.equalsIgnoreCase("true")) yield true;
                    else if (value.equalsIgnoreCase("false")) yield false;
                    throw new IllegalArgumentException(
                            "Type of value to insert " + value + " doesn't match required type Boolean for this column"
                    );
                }
                case BIGINT -> Long.parseLong(value);
                default -> value;
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Type of value to insert " + value + " doesn't match required type " + type.name() + " for this column"
            );
        }
    }
}
