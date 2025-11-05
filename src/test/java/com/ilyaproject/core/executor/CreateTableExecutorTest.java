package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.CreateTableQuery;
import com.ilyaproject.core.dto.table.TableDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateTableExecutorTest {
    private CreateTableExecutor executor;
    private Database db;
    private final String successMessage = "Table created";
    private final String failureMessage = "Failed to create table ";

    @BeforeEach
    void setUp() {
        executor = new CreateTableExecutor();
        db = Database.getInstance();
    }

    @Test
    void createTableQuery_executeCreateTable_returnSuccessfulSQLResponse() {
        // Given
        String name = "products";
        Map<String, JsqlType> schema = Map.of(
                "id", JsqlType.BIGINT,
                "name", JsqlType.TEXT,
                "price", JsqlType.INTEGER,
                "category", JsqlType.TEXT,
                "stock", JsqlType.INTEGER
        );
        CreateTableQuery query = new CreateTableQuery(name, schema);

        // When
        SQLResponse response = executor.get(query, db);
        TableDto createdTable = getTable(name, db);

        // Then
        assertTrue(response.success());
        assertEquals(successMessage, response.responseMessage());
        assertTrue(response.data().isEmpty());

        assertEquals(schema, createdTable.schema());
        assertTrue(createdTable.rows().isEmpty());
    }

    @Test
    void createTableQueryWithNoFields_executeCreateTable_returnFailedSQLResponse() {
        // Given
        String name = "products";
        Map<String, JsqlType> schema = new HashMap<>();
        CreateTableQuery query = new CreateTableQuery(name, schema);

        // When
        SQLResponse response = executor.get(query, db);

        // Then
        assertFalse(response.success());
        assertEquals(failureMessage + "CREATE TABLE statement supposed to have schema", response.responseMessage());
        assertTrue(response.data().isEmpty());
    }

    @Test
    void createTableQueryWithTheSameNameThatAlreadyExists_executeCreateTable_returnFailedSQLResponse() {
        // Given
        String name = "products";
        Map<String, JsqlType> schema = Map.of(
                "id", JsqlType.BIGINT,
                "name", JsqlType.TEXT,
                "price", JsqlType.INTEGER,
                "category", JsqlType.TEXT,
                "stock", JsqlType.INTEGER
        );
        CreateTableQuery query = new CreateTableQuery(name, schema);

        // When
        TableUtils.createTable(query, db);
        SQLResponse response = executor.get(query, db);

        // Then
        assertFalse(response.success());
        assertEquals(failureMessage + "Table products already exists", response.responseMessage());
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
