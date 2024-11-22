package org.example.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.example.model.MigrationRecord;
import org.example.service.MigrationHistoryService;
import org.example.service.MigrationLockService;
import org.example.service.MigrationReportService;
import org.example.util.ConnectionManager;
import org.example.util.MigrationFileReader;
import org.example.util.MigrationPaths;
import org.example.util.MigrationRollbackGenerator;
import org.example.util.ReportPaths;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * MigrationExecutor handles the execution of database migrations.
 */
@Slf4j
public class MigrationExecutor {
    private final MigrationHistoryService historyService;
    private final MigrationLockService lockService;
    private final MigrationReportService reportService = new MigrationReportService();
    private final EssentialTableCreator tableCreator = new EssentialTableCreator();

    /**
     * Constructs a new MigrationExecutor with the specified history and lock services.
     *
     * @param historyService the migration history service
     * @param lockService    the migration lock service
     */
    public MigrationExecutor(MigrationHistoryService historyService, MigrationLockService lockService) {
        this.historyService = historyService;
        this.lockService = lockService;
    }

    /**
     * Applies all pending migrations and generates reports for the applied migrations.
     */
    public void migrate() {
        Connection connection = null;
        List<MigrationRecord> appliedThisRun = new ArrayList<>();
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false);

            tableCreator.createEssentialTablesIfNotExists(connection);

            if (lockService.isLocked(connection)) {
                log.warn("Migration is already in progress by another process.");
                return;
            }

            lockService.lock(connection);

            List<String> migrationFiles = MigrationFileReader.getMigrationFiles();
            List<String> appliedMigrations = historyService.getAppliedMigrations(connection);

            for (String file : migrationFiles) {
                if (appliedMigrations.contains(file)) {
                    log.info("Migration already applied: {}", file);
                    continue;
                }

                applyMigration(file, connection, appliedThisRun);
            }

            MigrationRollbackGenerator.generateRollbackFiles(MigrationPaths.ROLLBACK_DIRECTORY);

            connection.commit();

            reportService.generateJSONReport(appliedThisRun, ReportPaths.MIGRATE_REPORT_DIRECTORY + "migration_report.json");

            lockService.unlock(connection);
        } catch (SQLException e) {
            handleMigrationException(connection, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Applies a single migration file.
     *
     * @param file            the migration file
     * @param connection      the database connection
     * @param appliedThisRun  the list of applied migration records in this run
     */
    private void applyMigration(String file, Connection connection, List<MigrationRecord> appliedThisRun) {
        try {
            String sql = MigrationFileReader.readMigrationFile(file);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                historyService.recordMigration(connection, file);
                appliedThisRun.add(new MigrationRecord(file, "SUCCESS", new java.sql.Timestamp(System.currentTimeMillis())));
                log.info("Successfully applied migration: {}", file);
            } catch (SQLException e) {
                appliedThisRun.add(new MigrationRecord(file, "FAILED", new java.sql.Timestamp(System.currentTimeMillis())));
                connection.rollback(); // Rollback transaction in case of failure
                lockService.unlock(connection); // Release lock
                log.error("Failed to apply migration: {}. Rolled back all changes.", file, e);
                throw new RuntimeException("Critical error during migration application", e);
            }
        } catch (SQLException e) {
            log.error("Failed to apply migration: {}", file, e);
            throw new RuntimeException("Critical error during migration application", e);
        }
    }

    /**
     * Handles exceptions during the migration process.
     *
     * @param connection the database connection
     * @param e          the exception thrown
     */
    private void handleMigrationException(Connection connection, Exception e) {
        if (connection != null) {
            try {
                connection.rollback();
                lockService.unlock(connection);
            } catch (SQLException rollbackEx) {
                log.error("Failed to rollback transaction", rollbackEx);
                throw new RuntimeException("Critical error during transaction rollback", rollbackEx);
            }
        }
        log.error("Migration process failed", e);
        throw new RuntimeException("Critical error during migration process", e);
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
