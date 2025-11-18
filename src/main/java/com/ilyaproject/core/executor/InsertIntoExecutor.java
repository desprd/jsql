package com.ilyaproject.core.executor;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.db.TableUtils;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.InsertIntoQuery;

import java.util.Optional;

public class InsertIntoExecutor implements StatementExecutor<InsertIntoQuery>{

    @Override
    public SQLResponse get(InsertIntoQuery query, Database db) {
        try {
            TableUtils.insertIntoTable(query, db);
            return new SQLResponse(
                    true,
                    "Row inserted",
                    Optional.empty()
            );
        } catch (Exception e) {
            return new SQLResponse(
                    false,
                    "Failed to insert row " + e.getMessage(),
                    Optional.empty()
            );
        }
    }
}
