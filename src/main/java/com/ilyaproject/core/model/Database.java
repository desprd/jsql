package com.ilyaproject.core.model;

import com.ilyaproject.core.dto.TableDto;
import com.ilyaproject.core.model.type.JsqlType;
import com.ilyaproject.core.utils.DataUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class Database {

    private static volatile Database instance;
    private final Map<String, Table> tables;

    private Database() {
        tables = new HashMap<>();
    }

    public static Database getInstance() {
        Database result = instance;
        if (result != null) {
            return result;
        }
        synchronized (Database.class) {
            if (instance == null) {
                instance = new Database();
            }
            return instance;
        }
    }

    public void createTable(String name, Map<String, JsqlType> schema) {
        tables.put(name, new Table(name, schema));
    }

    public void insert(String name, Map<String, Object> rowData) {
        Table table = tables.get(name);
        if (table != null) {
            try {
                table.createRow(rowData);
            } catch (SQLException | IllegalArgumentException e) {
                System.err.println("FAILURE: " + e.getMessage());
            }
        } else {
            System.err.printf("FAILURE: table %s was not found in database%n", name);
        }
    }

    public TableDto select(String name) {
        Table table = tables.get(name);
        if (table != null){
            return table.getTableData();
        }
        return DataUtils.getEmptyTableDto();
    }
}
