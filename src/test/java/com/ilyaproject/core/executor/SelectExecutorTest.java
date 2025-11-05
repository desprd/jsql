package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.expression.*;
import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.dto.query.SelectQueryBuilder;
import com.ilyaproject.core.dto.table.TableDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SelectExecutorTest {
    private final SelectExecutor executor = new SelectExecutor();
    private SelectQuery query;
    private TableDto productsTable;
    private SimpleExpression leftExpr;
    private SimpleExpression rightExpr;

    @BeforeEach
    void setUp() {
        List<ExpressionUnit> leftUnits = List.of(
                new ExpressionUnit("price", ExpressionUnitType.TEXT),
                new ExpressionUnit(">", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("500", ExpressionUnitType.NUMERIC)
        );
        leftExpr = new SimpleExpression(leftUnits);

        List<ExpressionUnit> rightUnits = List.of(
                new ExpressionUnit("stock", ExpressionUnitType.TEXT),
                new ExpressionUnit(">", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("0", ExpressionUnitType.NUMERIC)
        );
        rightExpr = new SimpleExpression(rightUnits);

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

        productsTable = new TableDto(new HashMap<>(schema), new ArrayList<>(rows));
    }

    @Test
    void selectQueryAllColumnsAndNoConditions_executeSelect_returnSuccessfulSQLResponse() {
        // Given
        query = new SelectQueryBuilder()
                .addTable("products")
                .build();

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertTrue(response.success());
        assertTrue(response.data().isPresent());
        assertEquals(List.of(productsTable), response.data().get());

    }

    @Test
    void selectQueryChosenColumnsAndNoConditions_executeSelect_returnSuccessfulSQLResponse() {
        // Given
        query = new SelectQueryBuilder()
                .addTable("products")
                .addColumn("name")
                .addColumn("price")
                .build();
        List<Map<String, Object>> expectedRows = List.of(
                new HashMap<>(Map.of("name", "TV", "price", 1200.0)),
                new HashMap<>(Map.of("name", "Laptop", "price", 1500.0)),
                new HashMap<>(Map.of("name", "Phone", "price", 800.0)),
                new HashMap<>(Map.of("name", "Tablet", "price", 600.0)),
                new HashMap<>(Map.of("name", "Mouse", "price", 50.0)),
                new HashMap<>(Map.of("name", "Keyboard", "price", 70.0)),
                new HashMap<>(Map.of("name", "Headphones", "price", 200.0)),
                new HashMap<>(Map.of("name", "Smartwatch", "price", 900.0)),
                new HashMap<>(Map.of("name", "Camera", "price", 1800.0)),
                new HashMap<>(Map.of("name", "Monitor", "price", 400.0))
        );
        List<TableDto> expectedTable = List.of(new TableDto(Map.of("name", JsqlType.TEXT, "price", JsqlType.INTEGER), expectedRows));

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertTrue(response.success());
        assertTrue(response.data().isPresent());
        assertEquals(expectedTable, response.data().get());
    }

    @Test
    void selectQueryWithSimpleCondition_executeSelect_returnSuccessfulSQLResponse() {
        query = new SelectQueryBuilder()
                .addTable("products")
                .addColumn("name")
                .addColumn("price")
                .addConditions(leftExpr)
                .build();
        List<Map<String, Object>> expectedRows = List.of(
                new HashMap<>(Map.of("name", "TV", "price", 1200.0)),
                new HashMap<>(Map.of("name", "Laptop", "price", 1500.0)),
                new HashMap<>(Map.of("name", "Phone", "price", 800.0)),
                new HashMap<>(Map.of("name", "Tablet", "price", 600.0)),
                new HashMap<>(Map.of("name", "Smartwatch", "price", 900.0)),
                new HashMap<>(Map.of("name", "Camera", "price", 1800.0))
        );
        List<TableDto> expectedTable = List.of(new TableDto(Map.of("name", JsqlType.TEXT, "price", JsqlType.INTEGER), expectedRows));

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertTrue(response.success());
        assertTrue(response.data().isPresent());
        assertEquals(expectedTable, response.data().get());
    }

    @Test
    void selectQueryWithAndCondition_executeSelect_returnSuccessfulSQLResponse() {
        // Given
        ExpressionNode andNode = new ExpressionNode("AND", leftExpr, rightExpr);
        query = new SelectQueryBuilder()
                .addTable("products")
                .addColumn("name")
                .addColumn("price")
                .addConditions(andNode)
                .build();
        List<Map<String, Object>> expectedRows = List.of(
                new HashMap<>(Map.of("name", "TV", "price", 1200.0)),
                new HashMap<>(Map.of("name", "Laptop", "price", 1500.0)),
                new HashMap<>(Map.of("name", "Phone", "price", 800.0)),
                new HashMap<>(Map.of("name", "Smartwatch", "price", 900.0)),
                new HashMap<>(Map.of("name", "Camera", "price", 1800.0))
        );
        List<TableDto> expectedTable = List.of(new TableDto(Map.of("name", JsqlType.TEXT, "price", JsqlType.INTEGER), expectedRows));

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertTrue(response.success());
        assertTrue(response.data().isPresent());
        assertEquals(expectedTable, response.data().get());
    }

    @Test
    void selectQueryWithOrCondition_executeSelect_returnSuccessfulSQLResponse() {
        // Given
        ExpressionNode andNode = new ExpressionNode("OR", leftExpr, rightExpr);
        query = new SelectQueryBuilder()
                .addTable("products")
                .addColumn("name")
                .addColumn("price")
                .addConditions(andNode)
                .build();
        List<Map<String, Object>> expectedRows = List.of(
                new HashMap<>(Map.of("name", "TV", "price", 1200.0)),
                new HashMap<>(Map.of("name", "Laptop", "price", 1500.0)),
                new HashMap<>(Map.of("name", "Phone", "price", 800.0)),
                new HashMap<>(Map.of("name", "Tablet", "price", 600.0)),
                new HashMap<>(Map.of("name", "Smartwatch", "price", 900.0)),
                new HashMap<>(Map.of("name", "Camera", "price", 1800.0)),
                new HashMap<>(Map.of("name", "Mouse", "price", 50.0)),
                new HashMap<>(Map.of("name", "Headphones", "price", 200.0)),
                new HashMap<>(Map.of("name", "Monitor", "price", 400.0))
        );
        List<TableDto> expectedTable = List.of(new TableDto(Map.of("name", JsqlType.TEXT, "price", JsqlType.INTEGER), expectedRows));

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertTrue(response.success());
        assertTrue(response.data().isPresent());
        assertEquals(expectedTable, response.data().get());
    }

    @Test
    void selectQueryWithWrongFormatSimpleCondition_executeSelect_returnFailedSQLResponse() {
        // Given
        List<ExpressionUnit> units = List.of(
                new ExpressionUnit("id", ExpressionUnitType.TEXT),
                new ExpressionUnit(">", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("3", ExpressionUnitType.NUMERIC),
                new ExpressionUnit("<", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("price", ExpressionUnitType.TEXT)
        );
        SimpleExpression wrongFormatExpression = new SimpleExpression(units);
        query = new SelectQueryBuilder()
                .addTable("products")
                .addConditions(wrongFormatExpression)
                .build();
        String expectedMessage = "Failed to select: Wrong expression format in WHERE block";

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertFalse(response.success());
        assertTrue(response.data().isEmpty());
        assertEquals(expectedMessage, response.responseMessage());
    }

    @Test
    void selectQueryWithNotExistingFieldInSimpleCondition_executeSelect_returnFailedSQLResponse() {
        // Given
        List<ExpressionUnit> units = List.of(
                new ExpressionUnit("quality", ExpressionUnitType.TEXT),
                new ExpressionUnit(">", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("3", ExpressionUnitType.NUMERIC)
        );
        SimpleExpression wrongFormatExpression = new SimpleExpression(units);
        query = new SelectQueryBuilder()
                .addTable("products")
                .addConditions(wrongFormatExpression)
                .build();
        String expectedMessage = "Failed to select: WHERE expression contains field that wasn't found in table";

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertFalse(response.success());
        assertTrue(response.data().isEmpty());
        assertEquals(expectedMessage, response.responseMessage());
    }

    @Test
    void selectQueryWithUnknownSymbolInSimpleCondition_executeSelect_returnFailedSQLResponse() {
        // Given
        List<ExpressionUnit> units = List.of(
                new ExpressionUnit("price", ExpressionUnitType.TEXT),
                new ExpressionUnit("%", ExpressionUnitType.SYMBOL),
                new ExpressionUnit("3", ExpressionUnitType.NUMERIC)
        );
        SimpleExpression wrongFormatExpression = new SimpleExpression(units);
        query = new SelectQueryBuilder()
                .addTable("products")
                .addConditions(wrongFormatExpression)
                .build();
        String expectedMessage = "Failed to select: Operator % is not allowed";

        // When
        SQLResponse response;
        try (MockedStatic<TableUtils> utilities = mockStatic(TableUtils.class)) {
            utilities.when(() -> TableUtils.getTablesByTablesNames(any(), any())).thenReturn(List.of(productsTable));
            response = executor.get(query, Database.getInstance());
        }

        // Then
        assertFalse(response.success());
        assertTrue(response.data().isEmpty());
        assertEquals(expectedMessage, response.responseMessage());
    }

}












