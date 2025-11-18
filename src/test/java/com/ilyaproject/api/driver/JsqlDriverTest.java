package com.ilyaproject.api.driver;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.CreateTableQuery;
import com.ilyaproject.core.dto.table.TableDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public class JsqlDriverTest {
    private JsqlDriver driver;
    private String sqlStatement;

    @BeforeEach
    void setUp() {
        driver = new JsqlDriver();
    }

    @Test
    void createTableStatement_runJsqlDriver_returnSuccessfulSQLResponse() {
        // Given
        sqlStatement = "CREATE TABLE products(country_name TEXT, area BIGINT, population BIGINT)";

        // When
        SQLResponse response = driver.run(sqlStatement);

        // Then
        assertTrue(response.success());
        assertEquals("Table created", response.responseMessage());
        assertTrue(response.data().isEmpty());
    }

    @Test
    void selectFromStatement_runJsqlDriver_returnSuccessfulSQLResponse() {
        // Given
        sqlStatement = "SELECT * FROM products WHERE price > 1000";
        Map<String, JsqlType> schema = Map.of(
                "id", JsqlType.BIGINT,
                "name", JsqlType.TEXT,
                "price", JsqlType.INTEGER,
                "category", JsqlType.TEXT,
                "stock", JsqlType.INTEGER
        );

        List<Map<String, Object>> rows = List.of(
                new HashMap<>(Map.of("id", 1, "name", "TV", "price", 1200.0, "category", "Electronics", "stock", 5)),
                new HashMap<>(Map.of("id", 2, "name", "Laptop", "price", 1500.0, "category", "Computers", "stock", 3)),
                new HashMap<>(Map.of("id", 3, "name", "Phone", "price", 800.0, "category", "Electronics", "stock", 10)),
                new HashMap<>(Map.of("id", 4, "name", "Tablet", "price", 600.0, "category", "Electronics", "stock", 0)),
                new HashMap<>(Map.of("id", 5, "name", "Mouse", "price", 50.0, "category", "Accessories", "stock", 100)),
                new HashMap<>(Map.of("id", 6, "name", "Keyboard", "price", 70.0, "category", "Accessories", "stock", 0)),
                new HashMap<>(Map.of("id", 7, "name", "Headphones", "price", 200.0, "category", "Audio", "stock", 20)),
                new HashMap<>(Map.of("id", 8, "name", "Smartwatch", "price", 900.0, "category", "Wearables", "stock", 15)),
                new HashMap<>(Map.of("id", 9, "name", "Camera", "price", 1800.0, "category", "Photography", "stock", 5)),
                new HashMap<>(Map.of("id", 10, "name", "Monitor", "price", 400.0, "category", "Computers", "stock", 12))
        );
        List<Map<String, Object>> expectedRows = List.of(
                new HashMap<>(Map.of("id", 1, "name", "TV", "price", 1200.0, "category", "Electronics", "stock", 5)),
                new HashMap<>(Map.of("id", 2, "name", "Laptop", "price", 1500.0, "category", "Computers", "stock", 3)),
                new HashMap<>(Map.of("id", 9, "name", "Camera", "price", 1800.0, "category", "Photography", "stock", 5))

        );
        List<TableDto> expectedTables = List.of(new TableDto(schema, expectedRows));
        var productsTable = new TableDto(new HashMap<>(schema), new ArrayList<>(rows));

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = driver.run(sqlStatement);
        }

        // Then
        assertTrue(response.success());
        assertEquals("Selected", response.responseMessage());
        assertTrue(response.data().isPresent());
        assertEquals(expectedTables, response.data().get());
    }

    @Test
    void insertIntoStatement_runJsqlDriver_returnSuccessfulSQLResponse() {
        // Given
        Database db = Database.getInstance();
        Map<String, JsqlType> schema = new HashMap<>();
        schema.put("first_field", JsqlType.TEXT);
        schema.put("second_field", JsqlType.TEXT);
        CreateTableQuery query = new CreateTableQuery("tables", schema);
        sqlStatement = "INSERT INTO tables (first_field, second_field) VALUES ('something_1', 'something_2')";

        // When
        TableUtils.createTable(query, db);
        SQLResponse response = driver.run(sqlStatement);

        // Then
        assertTrue(response.success());
        assertEquals("Row inserted", response.responseMessage());
        assertTrue(response.data().isEmpty());
    }

}
