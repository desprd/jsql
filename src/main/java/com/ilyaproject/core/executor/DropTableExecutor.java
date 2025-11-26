package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.DropTableQuery;

import java.util.Optional;

public class DropTableExecutor implements StatementExecutor<DropTableQuery>{

    @Override
    public SQLResponse get(DropTableQuery query, Database db) {
        try {
            TableUtils.removeTableByName(query.tableName(), db);
            return new SQLResponse(
                    true,
                    "Table dropped",
                    Optional.empty()
            );
        } catch (Exception e) {
            return new SQLResponse(
                    false,
                    "Failed to drop table " + e.getMessage(),
                    Optional.empty()
            );
        }
    }
}
