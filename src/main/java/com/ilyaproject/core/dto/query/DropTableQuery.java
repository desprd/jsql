package com.ilyaproject.core.dto.query;

public record DropTableQuery(String tableName) implements SQLQuery {}
