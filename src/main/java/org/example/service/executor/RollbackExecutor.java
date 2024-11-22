package org.example.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.example.model.MigrationRecord;
import org.example.service.MigrationHistoryService;
import org.example.service.MigrationReportService;
import org.example.util.ConnectionManager;
import org.example.util.MigrationFileReader;
import org.example.util.ReportPaths;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * RollbackExecutor handles the rollback of database migrations.
 */
@Slf4j
public class RollbackExecutor {
    private final MigrationHistoryService historyService;
    private final MigrationReportService reportService = new MigrationReportService();

    /**
     * Constructs a new RollbackExecutor with the specified history service.
     *
     * @param historyService the migration history service
     */
    public RollbackExecutor(MigrationHistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * Rolls back the last applied migration and generates reports for the rollbacks.
     */
    public void rollback() {
        Connection connection = null;
        List<MigrationRecord> rollbackThisRun = new ArrayList<>();
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false);

            List<String> appliedMigrations = historyService.getAppliedMigrations(connection);
            if (!appliedMigrations.isEmpty()) {
                String firstMigration = appliedMigrations.remove(0);
                rollbackMigration(firstMigration, connection, rollbackThisRun);
                connection.commit();

                reportService.generateJSONReport(rollbackThisRun, ReportPaths.ROLLBACK_REPORT_DIRECTORY + "rollback_report.json");

                log.info("Successfully rolled back migration: {}", firstMigration);
            } else {
                log.info("No migrations to rollback.");
            }
        } catch (SQLException e) {
            handleRollbackException(connection, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Rolls back migrations to the specified target version and generates reports for the rollbacks.
     *
     * @param targetVersion the target version to rollback to
     */
    public void rollback(String targetVersion) {
        Connection connection = null;
        List<MigrationRecord> rollbackThisRun = new ArrayList<>();
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false);
            List<String> migrationsToRollback = historyService.getMigrationsToRollback(connection, targetVersion);
            for (String migrationFile : migrationsToRollback) {
                rollbackMigration(migrationFile, connection, rollbackThisRun);
            }
            connection.commit();
            reportService.generateJSONReport(rollbackThisRun, ReportPaths.ROLLBACK_REPORT_DIRECTORY + "rollback_report.json");
            log.info("Successfully rolled back to version: {}", targetVersion);
        } catch (SQLException e) {
            handleRollbackException(connection, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Rolls back a single migration file.
     *
     * @param firstMigration  the first migration file to rollback
     * @param connection      the database connection
     * @param rollbackThisRun the list of rollback migration records in this run
     */
    private void rollbackMigration(String firstMigration, Connection connection, List<MigrationRecord> rollbackThisRun) {
        try {
            String rollbackFile = firstMigration.replace(".sql", "_rollback.sql");
            String rollbackSql = MigrationFileReader.readMigrationFile(rollbackFile);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(rollbackSql);
            }
            historyService.removeMigrationRecord(connection, firstMigration);
            rollbackThisRun.add(new MigrationRecord(firstMigration, "ROLLED BACK", new java.sql.Timestamp(System.currentTimeMillis())));
        } catch (SQLException e) {
            log.error("Failed to rollback migration: {}", firstMigration, e);
            throw new RuntimeException("Critical error during migration rollback", e);
        }
    }

    /**
     * Handles exceptions during the rollback process.
     *
     * @param connection the database connection
     * @param e          the exception thrown
     */
    private void handleRollbackException(Connection connection, Exception e) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("Failed to rollback transaction", rollbackEx);
                throw new RuntimeException("Critical error during transaction rollback", rollbackEx);
            }
        }
        log.error("Rollback process failed", e);
        throw new RuntimeException("Critical error during rollback process", e);
    }

    /**
     * Closes the database connection.
     *
     * @param connection the database connection
     */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close connection", e);
                throw new RuntimeException("Critical error closing database connection", e);
            }
        }
    }
}
