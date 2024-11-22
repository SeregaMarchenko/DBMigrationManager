package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.migration.MigrationManager;

import java.util.Scanner;

/**
 * Main class for interacting with the migration tool via command line.
 */
@Slf4j
public class MigrationTool {
    public static void main(String[] args) {
        MigrationManager migrationManager = new MigrationManager();
        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equalsIgnoreCase("exit")) {
            log.info("Please enter a command: 'migrate', 'rollback <version>', 'status', or 'exit' to quit.");
            command = scanner.nextLine().trim();

            if (command.toLowerCase().startsWith("rollback ")) {
                String version = command.substring(9).trim();
                try {
                    int versionNumber = Integer.parseInt(version);
                    int currentVersion = migrationManager.getCurrentVersion();

                    if (versionNumber >= 0 && versionNumber <= currentVersion) {
                        migrationManager.rollback(version);
                    } else {
                        log.info("Invalid version number. Please enter a number between 0 and " + currentVersion + ".");
                    }
                } catch (NumberFormatException e) {
                    log.info("Invalid version format. Please enter a valid integer.");
                }
            } else {
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
                        log.info("Unknown command. Please try again.");
                }
            }
        }
        scanner.close();
    }
}
