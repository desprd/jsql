package com.ilyaproject.core.utils;

import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.table.TableDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorUtilsTest {

    @Test
    void listOfTableDto_deepCopy_correctMutableDeepCopyOfTableDtoList() {
        // Given
        Map<String, JsqlType> schema = new HashMap<>();
        schema.put("field1", JsqlType.TEXT);
        schema.put("field2", JsqlType.TEXT);

        Map<String, Object> row = new HashMap<>();
        row.put("field1", "text");
        row.put("field2", "text");

        List<TableDto> tables = new ArrayList<>();
        tables.add(new TableDto(schema, new ArrayList<>(List.of(row))));

        // When
        List<TableDto> tablesCopy = ExecutorUtils.deepCopy(tables);
        tablesCopy.getFirst().schema().remove("field1");
        tablesCopy.getFirst().rows().getFirst().put("field1", "update");

        // Then
        assertNotNull(tables.getFirst().schema().get("field1"));
        assertNull(tablesCopy.getFirst().schema().get("field1"));
        assertNotEquals(
                tablesCopy.getFirst().rows().getFirst().get("field1"),
                tables.getFirst().rows().getFirst().get("field1")
        );
    }
}
