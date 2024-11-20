package org.example.service;

import org.example.util.ConnectionManager;
import org.example.util.MigrationFileReader;
import org.example.util.MigrationRollbackGenerator;
import org.example.service.model.MigrationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MigrationService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationService.class);

    private final MigrationHistoryService historyService;
    private final MigrationLockService lockService;
    private final MigrationReportService reportService = new MigrationReportService();

    public MigrationService(MigrationHistoryService historyService, MigrationLockService lockService) {
        this.historyService = historyService;
        this.lockService = lockService;
    }

    public void migrate() {
        Connection connection = null;
        List<MigrationRecord> appliedThisRun = new ArrayList<>();
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // Create essential tables if they do not exist
            createEssentialTablesIfNotExists(connection);

            if (lockService.isLocked(connection)) {
                logger.warn("Migration is already in progress by another process.");
                return;
            }

            lockService.lock(connection); // Acquire lock

            List<String> migrationFiles = MigrationFileReader.getMigrationFiles();
            List<String> appliedMigrations = historyService.getAppliedMigrations(connection);

            for (String file : migrationFiles) {
                if (appliedMigrations.contains(file)) {
                    logger.info("Migration already applied: {}", file);
                    continue;
                }

                String sql = MigrationFileReader.readMigrationFile(file);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(sql);
                    historyService.recordMigration(connection, file);
                    appliedThisRun.add(new MigrationRecord(file, "SUCCESS", new java.sql.Timestamp(System.currentTimeMillis())));
                    logger.info("Successfully applied migration: {}", file);
                } catch (SQLException e) {
                    appliedThisRun.add(new MigrationRecord(file, "FAILED", new java.sql.Timestamp(System.currentTimeMillis())));
                    connection.rollback(); // Rollback transaction in case of failure
                    lockService.unlock(connection); // Release lock
                    logger.error("Failed to apply migration: {}. Rolled back all changes.", file, e);
                    return;
                }
            }

            // Generate rollbacks after all migrations are applied
            MigrationRollbackGenerator.generateRollbackFiles("src/main/resources/db/migrations");

            connection.commit(); // Commit transaction if all is successful

            // Generate reports for migrations applied in this run
            reportService.generateCSVReport(appliedThisRun, "migration_report.csv");
            reportService.generateJSONReport(appliedThisRun, "migration_report.json");

            lockService.unlock(connection); // Release lock
        } catch (IOException | SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback transaction in case of error
                    lockService.unlock(connection); // Release lock
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            logger.error("Migration process failed", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Restore AutoCommit value
                    connection.close(); // Close connection
                } catch (SQLException e) {
                    logger.error("Failed to close connection", e);
                }
            }
        }
    }

    public void rollback() {
        Connection connection = null;
        List<MigrationRecord> rollbackThisRun = new ArrayList<>();
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false); // Start transaction

            List<String> appliedMigrations = historyService.getAppliedMigrations(connection);
            if (!appliedMigrations.isEmpty()) {
                String lastMigration = appliedMigrations.get(appliedMigrations.size() - 1); // Get last applied migration
                String rollbackFile = lastMigration.replace(".sql", "_rollback.sql");
                String rollbackSql = MigrationFileReader.readMigrationFile(rollbackFile);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(rollbackSql);
                }
                historyService.removeMigrationRecord(connection, lastMigration);
                rollbackThisRun.add(new MigrationRecord(lastMigration, "ROLLED BACK", new java.sql.Timestamp(System.currentTimeMillis())));
                connection.commit(); // Commit transaction

                // Generate reports for rollbacks performed in this run
                reportService.generateCSVReport(rollbackThisRun, "rollback_report.csv");
                reportService.generateJSONReport(rollbackThisRun, "rollback_report.json");

                logger.info("Successfully rolled back migration: {}", lastMigration);
            } else {
                logger.info("No migrations to rollback.");
            }
        } catch (IOException | SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback transaction in case of error
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            logger.error("Rollback process failed", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Restore AutoCommit value
                    connection.close(); // Close connection
                } catch (SQLException e) {
                    logger.error("Failed to close connection", e);
                }
            }
        }
    }

    public void printMigrationStatus() {
        Connection connection = null;
        try {
            connection = ConnectionManager.getConnection();
            List<String> appliedMigrations = historyService.getAppliedMigrations(connection);
            String currentVersion = appliedMigrations.isEmpty() ? "No migrations applied" : appliedMigrations.get(appliedMigrations.size() - 1);

            logger.info("Current Database Version: {}", currentVersion);
            logger.info("Applied Migrations:");
            for (String migration : appliedMigrations) {
                logger.info(" - {}", migration);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve migration status", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Failed to close connection", e);
                }
            }
        }
    }

    private void createEssentialTablesIfNotExists(Connection connection) throws SQLException {
        String createMigrationHistoryTable = "CREATE TABLE IF NOT EXISTS migration_history (" +
                "id SERIAL PRIMARY KEY, " +
                "version VARCHAR(50) NOT NULL, " +
                "script_name VARCHAR(255) NOT NULL, " +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String createMigrationLockTable = "CREATE TABLE IF NOT EXISTS migration_lock (" +
                "id SERIAL PRIMARY KEY, " +
                "locked BOOLEAN NOT NULL, " +
                "locked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String createOperationLogTable = "CREATE TABLE IF NOT EXISTS operation_log (" +
                "id SERIAL PRIMARY KEY, " +
                "operation VARCHAR(50) NOT NULL, " +
                "details TEXT, " +
                "executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createMigrationHistoryTable);
            stmt.execute(createMigrationLockTable);
            stmt.execute(createOperationLogTable);
        }
    }
}
