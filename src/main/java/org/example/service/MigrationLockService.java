package org.example.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Service class for managing migration locks in the database.
 */
public class MigrationLockService {

    /**
     * Checks if the migration process is currently locked.
     *
     * @param connection the database connection
     * @return true if the migration process is locked, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isLocked(Connection connection) throws SQLException {
        String sql = "SELECT locked FROM migration_lock WHERE id = 1";
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean("locked");
            }
        }
        return false;
    }

    /**
     * Locks the migration process to prevent concurrent execution.
     *
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    public void lock(Connection connection) throws SQLException {
        String sql = "INSERT INTO migration_lock (id, locked) VALUES (1, TRUE) ON CONFLICT (id) DO UPDATE SET locked = EXCLUDED.locked, locked_at = EXCLUDED.locked_at";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Unlocks the migration process to allow execution.
     *
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    public void unlock(Connection connection) throws SQLException {
        String sql = "UPDATE migration_lock SET locked = FALSE, locked_at = CURRENT_TIMESTAMP WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
