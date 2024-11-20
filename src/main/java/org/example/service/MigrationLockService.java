package org.example.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationLockService {

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

    public void lock(Connection connection) throws SQLException {
        String sql = "INSERT INTO migration_lock (id, locked) VALUES (1, TRUE) ON CONFLICT (id) DO UPDATE SET locked = EXCLUDED.locked, locked_at = EXCLUDED.locked_at";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void unlock(Connection connection) throws SQLException {
        String sql = "UPDATE migration_lock SET locked = FALSE, locked_at = CURRENT_TIMESTAMP WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
