package org.example.service;

import org.example.service.executor.MigrationExecutor;
import org.example.service.executor.MigrationStatusPrinter;
import org.example.service.executor.RollbackExecutor;

public class MigrationService {

    private final MigrationExecutor migrationExecutor;
    private final RollbackExecutor rollbackExecutor;
    private final MigrationStatusPrinter statusPrinter;

    /**
     * Constructs a new MigrationService with the specified history and lock services.
     *
     * @param historyService the migration history service
     * @param lockService    the migration lock service
     */
    public MigrationService(MigrationHistoryService historyService, MigrationLockService lockService) {
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
     * Prints the current migration status of the database.
     */
    public void printMigrationStatus() {
        statusPrinter.printMigrationStatus();
    }
}
