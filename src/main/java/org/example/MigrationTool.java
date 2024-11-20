package org.example;

import org.example.migration.MigrationManager;

public class MigrationTool {
    public static void main(String[] args) {
        MigrationManager migrationManager = new MigrationManager();

        if (args.length > 0) {
            switch (args[0]) {
                case "migrate":
                    migrationManager.migrate();
                    break;
                case "rollback":
                    migrationManager.rollback();
                    break;
                case "status":
                    migrationManager.printStatus();
                    break;
                default:
                    System.out.println("Unknown command. Use 'migrate', 'rollback', or 'status'.");
            }
        } else {
            System.out.println("Please provide a command: 'migrate', 'rollback', or 'status'.");
        }
    }
}
