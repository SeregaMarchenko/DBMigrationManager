package org.example.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing migration locks in the database.
 */
@Slf4j
public class MigrationLockService {

    /**
     * Checks if the migration process is currently locked.
     *
     * @param connection the database connection
     * @return true if the migration process is locked, false otherwise
     */
    public boolean isLocked(Connection connection) {
        String sql = "SELECT locked FROM migration_lock WHERE id = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                boolean locked = rs.getBoolean("locked");
                log.info("Lock status: {}", locked);
                return locked;
            }
        } catch (SQLException e) {
            log.error("Error checking lock status", e);
            throw new RuntimeException("Critical error while checking lock status", e);
        }
        return false;
    }

    /**
     * Locks the migration process to prevent concurrent migrations.
     *
     * @param connection the database connection
     */
    public void lock(Connection connection) {
        String sql = "INSERT INTO migration_lock (id, locked) VALUES (1, TRUE) " +
                "ON CONFLICT (id) DO UPDATE SET locked = EXCLUDED.locked, locked_at = CURRENT_TIMESTAMP";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Process locked");
        } catch (SQLException e) {
            log.error("Error locking process", e);
            throw new RuntimeException("Critical error while locking process", e);
        }
    }

    /**
     * Unlocks the migration process to allow future migrations.
     *
     * @param connection the database connection
     */
    public void unlock(Connection connection) {
        String sql = "UPDATE migration_lock SET locked = FALSE, locked_at = CURRENT_TIMESTAMP WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Process unlocked");
        } catch (SQLException e) {
            log.error("Error unlocking process", e);
            throw new RuntimeException("Critical error while unlocking process", e);
        }
    }
}
