package com.ilyaproject.core.dto.expression;

import java.util.List;

public record SimpleExpression(
        List<ExpressionUnit> expression
) implements Expression {}
