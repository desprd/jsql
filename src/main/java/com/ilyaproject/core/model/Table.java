package com.ilyaproject.core.model;



import com.ilyaproject.core.dto.TableDto;
import com.ilyaproject.core.model.type.JsqlType;
import com.ilyaproject.core.model.type.TypesTable;

import java.sql.SQLException;
import java.util.*;

public final class Table {

    private final String name;
    private final Schema schema;
    private final List<Row> rows;

    public Table(
            String name,
            Map<String, JsqlType> schemaMap
    ) {
        this.name = name;
        this.schema = new Schema(schemaMap);
        rows = new ArrayList<>();
    }

    private class Schema {
        private final Map<String, JsqlType> columns;

        public Schema(Map<String, JsqlType> columns) {
            this.columns = columns;
        }

        public Set<String> getColumnsKeys() {
            return columns.keySet();
        }

        public JsqlType getType(String key) {
            return columns.get(key);
        }

    }

    private class Row {
        private Map<String, Object> elements;

        public Row() {
            elements = new HashMap<>();
            for (String key: schema.getColumnsKeys()) {
                elements.put(key, null);
            }
        }

        public void setElement(String key, Object element) throws SQLException, IllegalArgumentException{
            if (!elements.containsKey(key)) {
                throw new SQLException(
                        String.format(
                                "Table %s doesn't contains key %s in its schema",
                                name,
                                key
                        )
                );
            }
            checkElementType(key, element);
            elements.put(key, element);
        }

        private void checkElementType(String key, Object element) {
            JsqlType type = schema.getType(key);
            Class<?> requiredClass = TypesTable.convertTypes.get(type);
            if (!requiredClass.isInstance(element)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Type mismatch for element %s: %s field type is %s",
                                element,
                                key,
                                requiredClass.getSimpleName()
                        )
                );
            }
        }
    }

    public void createRow(Map<String, Object> rowData) throws SQLException, IllegalArgumentException {
        Row row = new Row();
        for (String key: rowData.keySet()) {
            row.setElement(key, rowData.get(key));
        }
        rows.add(row);
    }

    public TableDto getTableData() {
        return new TableDto(
                Map.copyOf(schema.columns),
                List.copyOf(rows
                        .stream()
                        .map(row -> row.elements)
                        .toList())
        );
    }

}
