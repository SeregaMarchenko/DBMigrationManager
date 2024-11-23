package org.example.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.example.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * EssentialTableCreator handles the creation of essential tables for migration management.
 */
@Slf4j
public class EssentialTableCreator {

    /**
     * Creates essential tables (migration_history and migration_lock) if they do not exist.
     * This method ensures that the necessary tables for migration management are present in the database.
     */
    public void createEssentialTablesIfNotExists() {
        String createMigrationHistoryTable = """
                CREATE TABLE IF NOT EXISTS migration_history (
                    id SERIAL PRIMARY KEY,
                    version INTEGER NOT NULL,
                    script_name VARCHAR(255) NOT NULL,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createMigrationLockTable = """
                CREATE TABLE IF NOT EXISTS migration_lock (
                    id SERIAL PRIMARY KEY,
                    locked BOOLEAN NOT NULL,
                    locked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection connection = ConnectionManager.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(createMigrationHistoryTable);
            stmt.execute(createMigrationLockTable);
            log.info("Essential tables created successfully");
        } catch (SQLException e) {
            log.error("Error creating essential tables", e);
            throw new RuntimeException("Critical error while creating essential tables", e);
        }
    }
}
