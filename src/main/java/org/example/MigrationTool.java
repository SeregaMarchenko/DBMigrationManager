package org.example;

import org.example.migration.MigrationManager;

import java.util.Scanner;

public class MigrationTool {
    public static void main(String[] args) {
        MigrationManager migrationManager = new MigrationManager();
        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equalsIgnoreCase("exit")) {
            System.out.println("Please enter a command: 'migrate', 'rollback', 'status', or 'exit' to quit.");
            command = scanner.nextLine().trim();

            switch (command.toLowerCase()) {
                case "migrate":
                    migrationManager.migrate();
                    break;
                case "rollback":
                    migrationManager.rollback();
                    break;
                case "status":
                    migrationManager.printStatus();
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Unknown command. Please try again.");
            }
        }
        scanner.close();
    }
}

