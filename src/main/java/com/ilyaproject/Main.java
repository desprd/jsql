package com.ilyaproject;

import com.ilyaproject.core.model.Database;
import com.ilyaproject.core.model.type.JsqlType;
import com.ilyaproject.core.utils.CliUtils;

import java.util.Map;
public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        db.createTable("users", Map.of("id", JsqlType.INTEGER, "name", JsqlType.TEXT, "surname", JsqlType.TEXT));
        db.insert("users", Map.of("id", 1, "name", "Alice"));
        db.insert("pidory", Map.of("id", 1, "name", "Alice"));
        db.insert("users", Map.of("id", 2, "name", "Mark"));
        db.insert("users", Map.of("id", 3, "name", "Vlados"));
        db.insert("users", Map.of("id", 4, "name", "Nigger"));
        CliUtils.printTable(db.select("users"));
        CliUtils.printTable(db.select("pidory"));
    }
}