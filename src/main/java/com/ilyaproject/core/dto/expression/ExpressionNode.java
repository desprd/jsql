package com.ilyaproject.core.dto.expression;

public record ExpressionNode(
        String operator,
        Expression left,
        Expression right
) implements Expression {}
