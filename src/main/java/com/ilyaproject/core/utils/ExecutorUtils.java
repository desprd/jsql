package com.ilyaproject.core.utils;

import com.ilyaproject.core.dto.table.TableDto;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutorUtils {
    public static List<TableDto> deepCopy(List<TableDto> basedList) {
        return basedList.stream()
                .map(table -> new TableDto(
                        new HashMap<>(table.schema()),
                        table.rows().stream()
                                .map(row -> new HashMap<>(row))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
