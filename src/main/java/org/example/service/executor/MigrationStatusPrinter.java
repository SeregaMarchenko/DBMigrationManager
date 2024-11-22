package org.example.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.example.service.MigrationHistoryService;
import org.example.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * MigrationStatusPrinter handles printing the current migration status of the database.
 */
@Slf4j
public class MigrationStatusPrinter {
    private final MigrationHistoryService historyService;

    /**
     * Constructs a new MigrationStatusPrinter with the specified history service.
     *
     * @param historyService the migration history service
     */
    public MigrationStatusPrinter(MigrationHistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * Prints the current migration status of the database.
     */
    public void printMigrationStatus() {
        Connection connection = null;
        try {
            connection = ConnectionManager.getConnection();
            List<String> appliedMigrations = historyService.getAppliedMigrations(connection);
            String currentVersion = appliedMigrations.isEmpty() ? "No migrations applied" : String.valueOf(historyService.getCurrentVersion());

            log.info("Current Database Version: {}", currentVersion);
            log.info("Applied Migrations:");
            for (String migration : appliedMigrations) {
                log.info(" - {}", migration);
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Failed to close connection", e);
                    throw new RuntimeException("Critical error closing database connection", e);
                }
            }
        }
    }
}
