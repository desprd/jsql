package com.ilyaproject.core.utils;



import com.ilyaproject.core.dto.table.TableDto;
import com.ilyaproject.core.model.type.JsqlType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CliUtils {
    public static void printTable(TableDto tableData) {
        Map<String, JsqlType> schema = tableData.schema();
        List<Map<String, Object>> rows = tableData.rows();
        if (rows != null && !rows.isEmpty()) {
            printStraightHorizontalLine(schema.size());
            printHeader(-1, extractKeysFromRow(rows.getFirst()));
            printStraightHorizontalLine(schema.size());
            int index = 0;
            for (Map<String, Object> row: rows) {
                printHeader(index, extractValuesFromRow(row));
                printStraightHorizontalLine(schema.size());
                index++;
            }
        } else {
            printStraightHorizontalLine(schema.size());
            printHeader(-1, new ArrayList<>(schema.keySet()));
            printStraightHorizontalLine(schema.size());
        }
    }

    private static void printStraightHorizontalLine(int length) {
        int upperBound = 3 + length * 10;
        for (int i = 0; i < upperBound; i++){
            System.out.print("―");
        }
        System.out.println();
    }

    private static void printHeader(int index, List<String> data) {
        System.out.print("|");
        if (index == -1) {
            System.out.print("   ");
        }else {
            System.out.print(" " + index + " ");
        }
        System.out.print("|");
        for (String field: data) {
            int fieldLength = field.length();
            int overallIndent = 15 - fieldLength;
            if (overallIndent > 0) {
                int rightIndent;
                int leftIndent;
                if (overallIndent % 2 != 0) {
                    leftIndent = overallIndent / 2;
                    rightIndent = leftIndent + 1;
                }else {
                    rightIndent = leftIndent = overallIndent / 2;
                }
                System.out.print(" ".repeat(rightIndent) + field + " ".repeat(leftIndent));
            } else if (overallIndent < 0) {
                System.out.print(field.substring(0, 12) + "...");
            } else {
                System.out.print(field);
            }
            System.out.print("|");
        }
        System.out.println();
    }

    private static List<String> extractValuesFromRow(Map<String, Object> row) {
        return row
                .values()
                .stream()
                .map(Objects::toString)
                .toList();
    }

    private static List<String> extractKeysFromRow(Map<String, Object> row) {
        return row
                .keySet()
                .stream()
                .toList();
    }
}
