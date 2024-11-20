package org.example.migration;

import org.example.service.MigrationHistoryService;
import org.example.service.MigrationLockService;
import org.example.service.MigrationService;

public class MigrationManager {

    private final MigrationService migrationService;

    public MigrationManager() {
        MigrationHistoryService historyService = new MigrationHistoryService();
        MigrationLockService lockService = new MigrationLockService();
        this.migrationService = new MigrationService(historyService, lockService);
    }

    public void migrate() {
        migrationService.migrate();
    }

    public void rollback() {
        migrationService.rollback();
    }

    public void printStatus() {
        migrationService.printMigrationStatus();
    }
}
