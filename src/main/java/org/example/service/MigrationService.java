package org.example.service;

import org.example.service.executor.MigrationExecutor;
import org.example.service.executor.MigrationStatusPrinter;
import org.example.service.executor.RollbackExecutor;

/**
 * MigrationService handles the migration operations, including applying migrations,
 * rolling back migrations to a specific version, and printing the migration status.
 */
public class MigrationService {

    private final MigrationExecutor migrationExecutor;
    private final RollbackExecutor rollbackExecutor;
    private final MigrationStatusPrinter statusPrinter;
    private final MigrationHistoryService historyService;
    private final MigrationLockService lockService;

    /**
     * Constructs a new MigrationService with the specified history and lock services.
     */
    public MigrationService() {
        this.historyService = new MigrationHistoryService();
        this.lockService = new MigrationLockService();
        this.migrationExecutor = new MigrationExecutor(historyService, lockService);
        this.rollbackExecutor = new RollbackExecutor(historyService);
        this.statusPrinter = new MigrationStatusPrinter(historyService);
    }

    /**
     * Applies all pending migrations and generates reports for the applied migrations.
     */
    public void migrate() {
        migrationExecutor.migrate();
    }

    /**
     * Rolls back the last applied migration and generates reports for the rollbacks.
     */
    public void rollback() {
        rollbackExecutor.rollback();
    }

    /**
     * Rolls back migrations to the specified target version.
     *
     * @param targetVersion the target version to rollback to
     */
    public void rollback(String targetVersion) {
        rollbackExecutor.rollback(targetVersion);
    }

    /**
     * Prints the current migration status of the database.
     */
    public void printMigrationStatus() {
        statusPrinter.printMigrationStatus();
    }

    /**
     * Gets the current version of the migrations applied to the database.
     *
     * @return the current migration version
     */
    public int getCurrentVersion() {
        return historyService.getCurrentVersion();
    }
}
