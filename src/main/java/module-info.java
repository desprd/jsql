open module com.ilyaproject.core {
    requires java.sql;
    exports com.ilyaproject.core.model;
    exports com.ilyaproject.core.parser;
    exports com.ilyaproject.core.utils;
    exports com.ilyaproject.core.dto.expression;
    exports com.ilyaproject.core.dto.query;
    exports com.ilyaproject.core.dto.token;
    exports com.ilyaproject.core.dto.table;
}