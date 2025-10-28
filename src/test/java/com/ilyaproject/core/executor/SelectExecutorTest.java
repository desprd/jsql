package com.ilyaproject.core.executor;

import com.ilyaproject.core.dto.expression.Expression;
import com.ilyaproject.core.dto.expression.ExpressionUnit;
import com.ilyaproject.core.dto.expression.ExpressionUnitType;
import com.ilyaproject.core.dto.expression.SimpleExpression;
import com.ilyaproject.core.dto.query.SelectQuery;
import com.ilyaproject.core.dto.query.SelectQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class SelectExecutorTest {
    private SelectQuery query;

    @BeforeEach
    void setUp() {
        Expression expression = new SimpleExpression(
                List.of(
                        new ExpressionUnit("area", ExpressionUnitType.TEXT),
                        new ExpressionUnit(">", ExpressionUnitType.SYMBOL),
                        new ExpressionUnit("10000", ExpressionUnitType.NUMERIC)
                )
        );
        query = new SelectQueryBuilder()
                .addTable("countries")
                .addColumn("name")
                .addConditions(expression)
                .build();
    }
}
