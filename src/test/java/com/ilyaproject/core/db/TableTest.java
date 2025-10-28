package com.ilyaproject.core.db;

import com.ilyaproject.core.db.type.JsqlType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TableTest {

    private Table table;

    @BeforeEach
    void setUp() {
        String name = "demo";
        var schema = Map.of(
                "id", JsqlType.INTEGER,
                "name", JsqlType.TEXT
        );
        table = new Table(name, schema);
    }

    @Test
    void getRowDataMap_createRowMethod_rowSuccessfullyCreated() throws Exception{
        // Given
        Map<String, Object> rowData = Map.of(
                "id", 1,
                "name", "Alice"
        );

        // When
        table.createRow(rowData);

        // Then
        assertEquals(rowData, table.getTableData().rows().getFirst());
    }

    @Test
    void getRowDataMapWithKeyThatIsNotInSchema_createRowMethod_throwSQLException() throws Exception {
        // Given
        Map<String, Object> rowData = Map.of(
                "id", 1,
                "username", "Alice"
        );

        // When / Then
        SQLException e = assertThrows(
                SQLException.class,
                () -> table.createRow(rowData)
        );
        assertEquals("Table demo doesn't contains key username in its schema", e.getMessage());
    }

    @Test
    void getRowDataMapWithValueMismatch_createRowMethod_throwIllegalArgumentException() throws Exception {
        // Given
        Map<String, Object> rowData = Map.of(
                "id", "1",
                "name", "Alice"
        );

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> table.createRow(rowData)
        );
        assertEquals("Type mismatch for element 1: id field type is Integer", e.getMessage());
    }

}
