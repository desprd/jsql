package com.ilyaproject.core.db;

import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.table.TableDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableUtilsTest {

    private Map<String, Table> allTables;
    private Database db;

    @BeforeEach
    void setUp() throws Exception{
        Table demo = new Table(
                "countries", Map.of(
                "name", JsqlType.TEXT, "area", JsqlType.BIGINT
        ));
        demo.createRow(Map.of("name", "test", "area", 100000L));
        allTables = Map.of("countries", demo);
        db = mock();
        when(db.getTables()).thenReturn(allTables);
    }

    @Test
    void listOfTablesNames_getTablesByTablesNames_returnCorrectListOfTableDTOs() {
        // Given
        List<String> names = List.of("countries");

        // When
        List<TableDto> tables = TableUtils.getTablesByTablesNames(names, db);
        TableDto resultTable = tables.getFirst();

        // Then
        assertEquals(Map.of("name", JsqlType.TEXT, "area", JsqlType.BIGINT), resultTable.schema());
        assertEquals("test", resultTable.rows().getFirst().get("name"));
        assertEquals(100000L, resultTable.rows().getFirst().get("area"));
    }

    @Test
    void emptyListOfTablesNames_getTablesByTablesNames_throwIllegalArgumentException() {
        // Given
        List<String> names = new ArrayList<>();

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> TableUtils.getTablesByTablesNames(names, db)
        );
        assertEquals("SELECT statement supposed to include at least one table", e.getMessage());
    }

    @Test
    void listOfTablesNamesWithNonexistentName_getTablesByTablesNames_throwIllegalArgumentException() {
        // Given
        List<String> names = List.of("factories");

        // When / Then
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> TableUtils.getTablesByTablesNames(names, db)
        );
        assertEquals("Table with name factories was not found in database", e.getMessage());
    }

}
