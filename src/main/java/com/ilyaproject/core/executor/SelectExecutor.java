package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.db.type.JsqlType;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.expression.*;
import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.dto.table.TableDto;
import com.ilyaproject.core.utils.DataUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SelectExecutor implements StatementExecutor<SelectQuery>{
    @Override
    public SQLResponse get(SelectQuery query, Database db) {
        List<TableDto> tables = TableUtils.getTablesByTablesNames(query.tables(), db);
        tables = applyConditions(tables, query.conditions());
        extractRequiredColumns(tables, query.columns());
        return new SQLResponse(true, "SELECTED", Optional.of(tables));
    }

    private void extractRequiredColumns(List<TableDto> tables, List<String> columns) {
        if (columns.isEmpty()) {
            return;
        }

        for (TableDto table : tables) {
            table.schema().keySet().removeIf(key -> !columns.contains(key));
            for (Map<String, Object> row : table.rows()) {
                row.keySet().removeIf(key -> !columns.contains(key));
            }
        }
    }

    private List<TableDto> applyConditions(List<TableDto> tables, Expression conditions) {
        if (conditions == null) return tables;
        if (conditions instanceof ExpressionNode node) {
            List<TableDto> left = applyConditions(tables, node.left());
            List<TableDto> right = applyConditions(tables, node.right());
            return combineTables(left, right, node.operator());

        }
        if (conditions instanceof SimpleExpression simple) {
            return tables.stream()
                    .map(table -> filterTable(table, simple))
                    .collect(Collectors.toList());
        }
        return tables;
    }

    @SuppressWarnings("unchecked")
    private TableDto filterTable(TableDto table, SimpleExpression condition) {
        List<ExpressionUnit> expressionUnits = condition.expression();
        if (!isSimpleExpressionValid(expressionUnits)) {
            throw new IllegalArgumentException("Wrong expression format in WHERE block");
        }
        List<Map<String, Object>> filteredRows =  table.rows().stream()
                .filter(row -> rowIsValid(row, expressionUnits))
                .map(row -> new HashMap<>(row))
                .collect(Collectors.toList());
        return new TableDto(new HashMap<>(table.schema()), filteredRows);
    }

    private List<TableDto> combineTables(List<TableDto> left, List<TableDto> right, String operator) {
        switch (operator) {
            case "AND" -> {
                return left.stream().map(table -> {
                       TableDto same = matchTable(table, right);
                       return intersectRows(table, same);
                })
                .collect(Collectors.toList());
            }
            case "OR" -> {
                Map<Map<String, JsqlType>, TableDto> merged = new LinkedHashMap<>();

                Stream.concat(left.stream(), right.stream())
                        .forEach(table -> merged.merge(
                                table.schema(),
                                table,
                                this::mergeRows
                        ));

                return new ArrayList<>(merged.values());
            }
        }
        return left;
    }

    private boolean isSimpleExpressionValid(List<ExpressionUnit> expressionUnits) {
        if (
                expressionUnits.size() != 3 ||
                expressionUnits.getFirst().type() != ExpressionUnitType.TEXT ||
                expressionUnits.get(1).type() != ExpressionUnitType.SYMBOL ||
                (expressionUnits.get(2).type() != ExpressionUnitType.NUMERIC &&
                expressionUnits.get(2).type() != ExpressionUnitType.TEXT)
        ) {
            return false;
        }
        return true;
    }

    private boolean rowIsValid(Map<String, Object> row, List<ExpressionUnit> expressionUnits) {
        String fieldName = expressionUnits.getFirst().value();
        if (!row.containsKey(fieldName)) {
            throw new IllegalArgumentException("WHERE expression contains field that wasn't found in table");
        }
        String symbol = expressionUnits.get(1).value();
        Object fieldValue = row.get(fieldName);
        String comparisonValue = expressionUnits.get(2).value();
        switch (symbol) {
            case ">" -> {
                try {
                    double realValue = Double.parseDouble(comparisonValue);
                    return ((Number) fieldValue).doubleValue() > realValue;
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(
                            "Field " + fieldName + " with value " + fieldValue + " cannot be compared with >"
                    );
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Comparison value " + comparisonValue + " cannot be compared with >"
                    );
                }
            }
            case "<" -> {
                try {
                    double realValue = Double.parseDouble(comparisonValue);
                    return ((Number) fieldValue).doubleValue() < realValue;
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(
                            "Field " + fieldName + " with value " + fieldValue + " cannot be compared with <"
                    );
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Comparison value " + comparisonValue + " cannot be compared with <"
                    );
                }
            }
            case "=" -> {
                if (expressionUnits.get(2).type() == ExpressionUnitType.NUMERIC) {
                    try {
                        double realValue = Double.parseDouble(comparisonValue);
                        return ((Number) fieldValue).doubleValue() == realValue;
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException(
                                "Field " + fieldName + " with value " + fieldValue + " cannot be compared with ="
                        );
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "Comparison value " + comparisonValue + " cannot be compared with ="
                        );
                    }
                } else if (expressionUnits.get(2).type() == ExpressionUnitType.TEXT) {
                    return fieldValue.equals(comparisonValue);
                }
            }
            default -> throw new IllegalArgumentException("Operator " + symbol + " is not allowed");
        }
        return false;
    }

    private TableDto matchTable(TableDto table, List<TableDto> right) {
        for (TableDto rightTable: right) {
            if (rightTable.schema().equals(table.schema())) {
                return rightTable;
            }
        }
        return DataUtils.getEmptyTableDto();
    }

    private TableDto intersectRows(TableDto firstTable, TableDto secondTable) {
        List<Map<String, Object>> intersection = new ArrayList<>();
        for (Map<String, Object> row: firstTable.rows()) {
            if (secondTable.rows().contains(row)) {
                intersection.add(new HashMap<>(row));
            }
        }
        return new TableDto(firstTable.schema(), intersection);
    }

    private TableDto mergeRows(TableDto first, TableDto second) {
        List<Map<String, Object>> mergedRows = new ArrayList<>(first.rows());
        for (Map<String, Object> row : second.rows()) {
            if (!mergedRows.contains(row)) {
                mergedRows.add(new HashMap<>(row));
            }
        }
        return new TableDto(first.schema(), mergedRows);
    }

}
