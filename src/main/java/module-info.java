open module com.ilyaproject.api {
    requires java.sql;
    requires java.xml.crypto;
    exports com.ilyaproject.api.cli;
    exports com.ilyaproject.api.driver;
    exports com.ilyaproject.core.dto.executor;
    exports com.ilyaproject.core.dto.table;
    exports com.ilyaproject.core.db.type;
}