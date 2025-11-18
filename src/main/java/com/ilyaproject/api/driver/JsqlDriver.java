package com.ilyaproject.api.driver;

import com.ilyaproject.core.db.Database;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.query.SQLQuery;
import com.ilyaproject.core.dto.token.Token;
import com.ilyaproject.core.executor.SQLExecutor;
import com.ilyaproject.core.parser.SQLParser;
import com.ilyaproject.core.parser.SQLTokenizer;

import java.util.List;
import java.util.Optional;

/**
* Orchestrator class that combines Jsql core parts to
* execute raw sql queries
 */

public class JsqlDriver {

    private final SQLTokenizer tokenizer;
    private final SQLParser parser;
    private final SQLExecutor executor;
    private final Database db;

    public JsqlDriver() {
        tokenizer = new SQLTokenizer();
        parser = new SQLParser();
        executor = new SQLExecutor();
        db = Database.getInstance();
    }

    JsqlDriver(Database db) {
        tokenizer = new SQLTokenizer();
        parser = new SQLParser();
        executor = new SQLExecutor();
        this.db = db;
    }

    public SQLResponse run(String sqlStatement) {
        SQLResponse response;
        try {
            List<Token> tokens = tokenizer.tokenize(sqlStatement);
            SQLQuery query = parser.parseStatement(tokens);
            response = executor.execute(query, db);
            return response;
        } catch (Exception e) {
            response = new SQLResponse(
                    false,
                    "Failed to run sql statement " + e.getMessage(),
                    Optional.empty()
            );
        }
        return response;
    }
}
