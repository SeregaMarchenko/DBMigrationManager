package org.example.service.executor;

import lombok.extern.slf4j.Slf4j;

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
     *
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    public void createEssentialTablesIfNotExists(Connection connection) throws SQLException {
        String createMigrationHistoryTable = """
                CREATE TABLE IF NOT EXISTS migration_history (
                    id SERIAL PRIMARY KEY,
                    version VARCHAR(50) NOT NULL,
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
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createMigrationHistoryTable);
            stmt.execute(createMigrationLockTable);
        }
    }
}
