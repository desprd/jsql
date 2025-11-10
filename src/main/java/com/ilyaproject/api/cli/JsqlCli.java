package com.ilyaproject.api.cli;

import com.ilyaproject.api.driver.JsqlDriver;
import com.ilyaproject.core.dto.executor.SQLResponse;
import com.ilyaproject.core.dto.table.TableDto;
import com.ilyaproject.core.utils.CliUtils;

import java.util.Scanner;

public class JsqlCli {

    private final JsqlDriver jsqlDriver = new JsqlDriver();

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("JSQL CLI — type 'quit' to exit.");
            while (true) {
                System.out.print("jsql> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit")) break;
                if (input.isEmpty()) continue;

                try {
                    SQLResponse response = jsqlDriver.run(input);
                    if (response.data().isPresent() && !response.data().get().isEmpty()) {
                        for (TableDto table : response.data().get()) {
                            CliUtils.printTable(table);
                        }
                    } else {
                        System.out.println(response.responseMessage());
                    }
                } catch (Exception e) {
                    System.err.println("Execution error: " + e.getMessage());
                }
                System.out.println();
            }
        }
    }
}
