package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.CreateTableQuery;
import com.ilyaproject.core.dto.query.InsertIntoQuery;
import com.ilyaproject.core.dto.table.TableDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InsertIntoExecutorTest {
    private InsertIntoExecutor executor;
    private Database db;
    private InsertIntoQuery insertIntoQuery;

    @BeforeEach
    void setUp() {
        executor = new InsertIntoExecutor();
        db = Database.getInstance();
        Map<String, JsqlType> schema = new HashMap<>();
        schema.put("field1", JsqlType.TEXT);
        schema.put("field2", JsqlType.TEXT);
        CreateTableQuery query = new CreateTableQuery("table", schema);
        TableUtils.createTable(query, db);
    }

    @Test
    void insertIntoQuery_executeInsertInto_returnSuccessfulSQLResponse() {
        // Given
        insertIntoQuery = new InsertIntoQuery(
                "table",
                Map.of("field1", "something1",
                        "field2", "something2")
        );

        // When
        SQLResponse response = executor.get(insertIntoQuery, db);
        TableDto table = getTable("table", db);

        // Then
        assertTrue(response.success());
        assertEquals("Row inserted", response.responseMessage());
        assertTrue(response.data().isEmpty());
        assertEquals(1, table.rows().size());
    }

    @Test
    void insertIntoQueryWithNonExistingTable_executeInsertInto_returnFailedSQLResponse() {
        // Given
        insertIntoQuery = new InsertIntoQuery(
                "another_table",
                Map.of("field1", "something1",
                        "field2", "something2")
        );

        // When
        SQLResponse response = executor.get(insertIntoQuery, db);

        // Then
        assertFalse(response.success());
        assertEquals(
                "Failed to insert row Table another_table was not found in database",
                response.responseMessage()
        );
        assertTrue(response.data().isEmpty());
    }

    @Test
    void insertIntoQueryWithNonExistingColumn_executeInsertInto_returnFailedSQLResponse() {
        // Given
        insertIntoQuery = new InsertIntoQuery(
                "table",
                Map.of("field1", "something1",
                        "field2", "something2",
                        "field3", "something3")
        );

        // When
        SQLResponse response = executor.get(insertIntoQuery, db);

        // Then
        assertFalse(response.success());
        assertEquals(
                "Failed to insert row table table's schema doesn't contains column field3",
                response.responseMessage()
        );
        assertTrue(response.data().isEmpty());
    }

    @AfterEach
    void cleanUp() {
        Database.removeInstance();
    }

    TableDto getTable(String name, Database db) {
        return TableUtils.getTablesByTablesNames(List.of(name), db).getFirst();
    }
}
