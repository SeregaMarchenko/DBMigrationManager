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

    public boolean isLocked(Connection connection) throws SQLException {
        String sql = "SELECT locked FROM migration_lock WHERE id = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                boolean locked = rs.getBoolean("locked");
                log.info("Migration lock status: {}", locked);
                return locked;
            }
        } catch (SQLException e) {
            log.error("Error checking migration lock status", e);
            throw e;
        }
        return false;
    }

    public void lock(Connection connection) throws SQLException {
        String sql = "INSERT INTO migration_lock (id, locked) VALUES (1, TRUE) " +
                "ON CONFLICT (id) DO UPDATE SET locked = EXCLUDED.locked, locked_at = CURRENT_TIMESTAMP";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Migration process locked");
        } catch (SQLException e) {
            log.error("Error locking migration process", e);
            throw e;
        }
    }

    public void unlock(Connection connection) throws SQLException {
        String sql = "UPDATE migration_lock SET locked = FALSE, locked_at = CURRENT_TIMESTAMP WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Migration process unlocked");
        } catch (SQLException e) {
            log.error("Error unlocking migration process", e);
            throw e;
        }
    }
}
