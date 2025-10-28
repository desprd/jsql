package com.ilyaproject.core.executor;

import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.SQLQuery;
import com.ilyaproject.core.db.Database;

interface StatementExecutor<T extends SQLQuery>{
    SQLResponse get(T query, Database db);
}

