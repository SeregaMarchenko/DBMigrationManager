package org.example.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing migration history records in the database.
 */
public class MigrationHistoryService {

    /**
     * Records a new migration in the migration history.
     *
     * @param connection the database connection
     * @param migrationFile the name of the migration file
     * @throws SQLException if a database access error occurs
     */
    public void recordMigration(Connection connection, String migrationFile) throws SQLException {
        String insertMigrationRecord = "INSERT INTO migration_history (version, script_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertMigrationRecord)) {
            pstmt.setString(1, getVersionFromFileName(migrationFile));
            pstmt.setString(2, migrationFile);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves a list of applied migrations from the migration history.
     *
     * @param connection the database connection
     * @return a list of applied migration file names
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Removes a migration record from the migration history.
     *
     * @param connection the database connection
     * @param migrationFile the name of the migration file to remove
     * @throws SQLException if a database access error occurs
     */
    public void removeMigrationRecord(Connection connection, String migrationFile) throws SQLException {
        String deleteMigrationRecord = "DELETE FROM migration_history WHERE script_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteMigrationRecord)) {
            pstmt.setString(1, migrationFile);
            pstmt.executeUpdate();
        }
    }

    /**
     * Extracts the version from the migration file name.
     *
     * @param fileName the name of the migration file
     * @return the version extracted from the file name
     */
    private String getVersionFromFileName(String fileName) {
        return fileName.split("__")[0].replace("V", "");
    }
}
