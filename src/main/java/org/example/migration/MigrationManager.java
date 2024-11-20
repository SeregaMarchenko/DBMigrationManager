package org.example.migration;

import org.example.service.MigrationHistoryService;
import org.example.service.MigrationLockService;
import org.example.service.MigrationService;

/**
 * Manager class for handling migration operations.
 */
public class MigrationManager {

    private final MigrationService migrationService;

    /**
     * Constructs a new MigrationManager and initializes the MigrationService.
     */
    public MigrationManager() {
        MigrationHistoryService historyService = new MigrationHistoryService();
        MigrationLockService lockService = new MigrationLockService();
        this.migrationService = new MigrationService(historyService, lockService);
    }

    /**
     * Executes pending migrations.
     */
    public void migrate() {
        migrationService.migrate();
    }

    /**
     * Rolls back the last applied migration.
     */
    public void rollback() {
        migrationService.rollback();
    }

    /**
     * Prints the current migration status.
     */
    public void printStatus() {
        migrationService.printMigrationStatus();
    }
}
