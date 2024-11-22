package org.example.migration;

import org.example.service.MigrationHistoryService;
import org.example.service.MigrationLockService;
import org.example.service.MigrationService;
import lombok.extern.slf4j.Slf4j;

/**
 * This class manages the migration operations including applying migrations,
 * rolling back migrations to a specific version, and printing the migration status.
 */
@Slf4j
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
     * Applies all pending migrations.
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
     * Rolls back migrations to the specified target version.
     *
     * @param targetVersion the target version to rollback to
     */
    public void rollback(String targetVersion) {
        migrationService.rollback(targetVersion);
    }

    /**
     * Prints the current migration status of the database.
     */
    public void printStatus() {
        migrationService.printMigrationStatus();
    }

    /**
     * Gets the current version of the migrations applied to the database.
     *
     * @return the current migration version
     */
    public int getCurrentVersion() {
        return migrationService.getCurrentVersion();
    }
}
