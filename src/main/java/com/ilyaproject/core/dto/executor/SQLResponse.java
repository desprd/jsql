package com.ilyaproject.core.dto.executor;

import com.ilyaproject.core.dto.table.TableDto;

import java.util.List;
import java.util.Optional;

public record SQLResponse(
        boolean success,
        String responseMessage,
        Optional<List<TableDto>> data
) {}