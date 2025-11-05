package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.CreateTableQuery;

import java.util.Optional;

public class CreateTableExecutor implements StatementExecutor<CreateTableQuery>{
    @Override
    public SQLResponse get(CreateTableQuery query, Database db) {
        try {
            TableUtils.createTable(query, db);
            return new SQLResponse(
                    true,
                    "Table created",
                    Optional.empty()
            );
        } catch (Exception e) {
            return new SQLResponse(
                    false,
                    "Failed to create table " + e.getMessage(),
                    Optional.empty()
            );
        }
    }
}
