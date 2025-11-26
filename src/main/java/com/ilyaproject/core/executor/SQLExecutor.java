package com.ilyaproject.core.executor;

import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.*;
import com.ilyaproject.core.db.Database;

import java.sql.SQLException;
import java.util.Map;

public class SQLExecutor {

    private final Map<Class<? extends SQLQuery>, StatementExecutor<? extends SQLQuery>> EXECUTORS = Map.of(
        SelectQuery.class,
        new SelectExecutor(),
        CreateTableQuery.class,
        new CreateTableExecutor(),
        InsertIntoQuery.class,
        new InsertIntoExecutor(),
        DropTableQuery.class,
        new DropTableExecutor()
    );

    @SuppressWarnings("unchecked")
    public SQLResponse execute(SQLQuery query, Database db) throws SQLException{
        if (query == null) {
            throw new IllegalArgumentException("SQLQuery cannot be null");
        }
        StatementExecutor<SQLQuery> executor = (StatementExecutor<SQLQuery>) EXECUTORS.get(query.getClass());
        if (executor == null) {
            throw new SQLException(
                    "Statement executor for SQLQuery type " +
                    query.getClass().getSimpleName() +
                    " doesn't exists"
            );
        }
        return executor.get(query, db);
    }
}
