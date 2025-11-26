package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.CreateTableQuery;
import com.ilyaproject.core.dto.query.DropTableQuery;
import com.ilyaproject.core.dto.table.TableDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DropTableExecutorTest {
    private Database db;
    private DropTableQuery query;
    private DropTableExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new DropTableExecutor();
        db = Database.getInstance();
        Map<String, JsqlType> schema = new HashMap<>();
        schema.put("field1", JsqlType.TEXT);
        schema.put("field2", JsqlType.TEXT);
        CreateTableQuery query = new CreateTableQuery("table", schema);
        TableUtils.createTable(query, db);
    }

    @Test
    void getDropTableQuery_executeDropTable_returnSuccessfulSQLResponse() {
        // Given
        query = new DropTableQuery("table");

        // When
        SQLResponse response = executor.get(query, db);
        boolean isStillExists = TableUtils.tableExists("table", db);

        // Then
        assertTrue(response.success());
        assertEquals("Table dropped", response.responseMessage());
        assertTrue(response.data().isEmpty());
        assertFalse(isStillExists);
    }

    @Test
    void getDropTableQueryWithNonExistingTableName_executeDropTable_returnFailedSQLResponse() {
        // Given
        query = new DropTableQuery("another_table");

        // When
        SQLResponse response = executor.get(query, db);

        // Then
        assertFalse(response.success());
        assertEquals(
                "Failed to drop table Database doesn't contain table another_table",
                response.responseMessage()
        );
        assertTrue(response.data().isEmpty());
    }

    @AfterEach
    void cleanUp() {
        Database.removeInstance();
    }
}
