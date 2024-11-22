package org.example.service;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class MigrationHistoryService {

    /**
     * Records a new migration in the migration history.
     *
     * @param connection the database connection
     * @param migrationFile the name of the migration file
     */
    public void recordMigration(Connection connection, String migrationFile) {
        String insertMigrationRecord = "INSERT INTO migration_history (version, script_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertMigrationRecord)) {
            pstmt.setString(1, getVersionFromFileName(migrationFile));
            pstmt.setString(2, migrationFile);
            pstmt.executeUpdate();
            log.info("Recorded migration: {}", migrationFile);
        } catch (SQLException e) {
            log.error("Failed to record migration: " + migrationFile, e);
            throw new RuntimeException("Critical error while recording migration: " + migrationFile, e);
        }
    }

    /**
     * Retrieves a list of applied migrations from the migration history.
     *
     * @param connection the database connection
     * @return a list of applied migration file names
     */
    public List<String> getAppliedMigrations(Connection connection) {
        List<String> appliedMigrations = new ArrayList<>();
        String selectAppliedMigrations = "SELECT script_name FROM migration_history ORDER BY version DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectAppliedMigrations)) {
            while (rs.next()) {
                appliedMigrations.add(rs.getString("script_name"));
            }
            log.info("Retrieved applied migrations: {}", appliedMigrations);
        } catch (SQLException e) {
            log.error("Failed to retrieve applied migrations", e);
            throw new RuntimeException("Critical error while retrieving applied migrations", e);
        }
        return appliedMigrations;
    }

    /**
     * Removes a migration record from the migration history.
     *
     * @param connection the database connection
     * @param migrationFile the name of the migration file to remove
     */
    public void removeMigrationRecord(Connection connection, String migrationFile) {
        String deleteMigrationRecord = "DELETE FROM migration_history WHERE script_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteMigrationRecord)) {
            pstmt.setString(1, migrationFile);
            pstmt.executeUpdate();
            log.info("Removed migration record: {}", migrationFile);
        } catch (SQLException e) {
            log.error("Failed to remove migration record: " + migrationFile, e);
            throw new RuntimeException("Critical error while removing migration record: " + migrationFile, e);
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
