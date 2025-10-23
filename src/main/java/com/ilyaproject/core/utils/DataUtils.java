package com.ilyaproject.core.utils;

import com.ilyaproject.core.dto.table.TableDto;
import com.ilyaproject.core.model.type.JsqlType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUtils {

    public static TableDto getEmptyTableDto() {
        Map<String, JsqlType> emptySchema = new HashMap<>();
        List<Map<String, Object>> emptyRows = new ArrayList<>();
        return new TableDto(emptySchema, emptyRows);
    }

}
