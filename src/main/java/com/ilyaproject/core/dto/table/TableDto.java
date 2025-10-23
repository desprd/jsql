package com.ilyaproject.core.dto.table;


import com.ilyaproject.core.model.type.JsqlType;

import java.util.List;
import java.util.Map;

public record TableDto (
        Map<String, JsqlType> schema,
        List<Map<String, Object>> rows
) {}
