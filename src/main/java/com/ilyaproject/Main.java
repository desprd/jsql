package com.ilyaproject;

import com.ilyaproject.api.cli.JsqlCli;
import com.ilyaproject.core.db.Database;

public class Main {
    public static void main(String[] args) {
        JsqlCli cli = new JsqlCli();
        cli.run();
    }
}