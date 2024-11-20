package org.example.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MigrationHistoryService {

    public void recordMigration(Connection connection, String migrationFile) throws SQLException {
        String insertMigrationRecord = "INSERT INTO migration_history (version, script_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertMigrationRecord)) {
            pstmt.setString(1, getVersionFromFileName(migrationFile));
            pstmt.setString(2, migrationFile);
            pstmt.executeUpdate();
        }
    }

    public List<String> getAppliedMigrations(Connection connection) throws SQLException {
        List<String> appliedMigrations = new ArrayList<>();
        String selectAppliedMigrations = "SELECT script_name FROM migration_history ORDER BY version DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectAppliedMigrations)) {
            while (rs.next()) {
                appliedMigrations.add(rs.getString("script_name"));
            }
        }
        return appliedMigrations;
    }

    public void removeMigrationRecord(Connection connection, String migrationFile) throws SQLException {
        String deleteMigrationRecord = "DELETE FROM migration_history WHERE script_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteMigrationRecord)) {
            pstmt.setString(1, migrationFile);
            pstmt.executeUpdate();
        }
    }

    private String getVersionFromFileName(String fileName) {
        return fileName.split("__")[0].replace("V", "");
    }
}
