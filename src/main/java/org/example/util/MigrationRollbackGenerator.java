package org.example.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility class for generating rollback SQL scripts for migrations.
 */
@Slf4j
public class MigrationRollbackGenerator {

    /**
     * Generates rollback SQL files for the given migration files.
     * This method reads the migration files, generates corresponding rollback SQL scripts,
     * and writes them to new files if they do not already exist.
     */
    public static void generateRollbackFiles() {
        try {
            List<String> migrationFiles = MigrationFileReader.getMigrationFiles();

            for (String migrationFile : migrationFiles) {
                String rollbackFile = migrationFile.replace(".sql", "_rollback.sql");
                Path rollbackPath = Path.of(rollbackFile);
                if (Files.exists(rollbackPath)) {
                    String rollbackSql = generateRollbackSql(new String(Files.readAllBytes(Paths.get(migrationFile))));
                    Files.write(rollbackPath, rollbackSql.getBytes());
                    log.info("Generated rollback file: {}", rollbackFile);
                } else {
                    log.info("Rollback file already exists: {}", rollbackFile);
                }
            }
        } catch (IOException e) {
            log.error("Error generating rollback files", e);
            throw new RuntimeException("Critical error while generating rollback files", e);
        }
    }

    /**
     * Generates the rollback SQL script for the given migration SQL script.
     *
     * @param migrationSql the migration SQL script
     * @return the generated rollback SQL script
     */
    private static String generateRollbackSql(String migrationSql) {
        StringBuilder rollbackSql = new StringBuilder();

        if (migrationSql.contains("CREATE TABLE")) {
            String tableName = extractTableName(migrationSql, "CREATE TABLE");
            rollbackSql.append("DROP TABLE IF EXISTS ").append(tableName).append(" CASCADE;");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("ADD COLUMN")) {
            String tableName = extractTableName(migrationSql, "ALTER TABLE");
            String columnName = extractColumnName(migrationSql, "ADD COLUMN");
            rollbackSql.append("ALTER TABLE ").append(tableName).append(" DROP COLUMN IF EXISTS ").append(columnName).append(";");
        } else if (migrationSql.contains("INSERT INTO")) {
            String tableName = extractTableName(migrationSql, "INSERT INTO");
            String conditions = generateDeleteConditions(migrationSql);
            rollbackSql.append("DELETE FROM ").append(tableName).append(" WHERE role_name IN (").append(conditions).append(");");
        } else if (migrationSql.contains("UPDATE")) {
            String tableName = extractTableName(migrationSql, "UPDATE");
            String setClause = migrationSql.split("SET")[1].split("WHERE")[0].trim();
            String whereClause = migrationSql.split("WHERE")[1].trim();
            rollbackSql.append("UPDATE ").append(tableName).append(" SET ").append(setClause).append(" WHERE ").append(whereClause);
        } else if (migrationSql.contains("DELETE FROM")) {
            String tableName = extractTableName(migrationSql, "DELETE FROM");
            rollbackSql.append("INSERT INTO ").append(tableName).append(" (SELECT * FROM change_log WHERE table_name = '")
                    .append(tableName).append("' AND operation = 'DELETE' ORDER BY change_time DESC LIMIT 1);");
        } else if (migrationSql.contains("CREATE INDEX")) {
            String indexName = migrationSql.split("CREATE INDEX")[1].split(" ON ")[0].trim();
            rollbackSql.append("DROP INDEX IF EXISTS ").append(indexName).append(";");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("DROP COLUMN")) {
            String tableName = extractTableName(migrationSql, "ALTER TABLE");
            String columnName = migrationSql.split("DROP COLUMN")[1].trim();
            rollbackSql.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN ").append(columnName).append(" TYPE COLUMN_TYPE;");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("RENAME COLUMN")) {
            String tableName = extractTableName(migrationSql, "ALTER TABLE");
            String oldColumnName = migrationSql.split("RENAME COLUMN")[1].split("TO")[0].trim();
            String newColumnName = migrationSql.split("TO")[1].trim();
            rollbackSql.append("ALTER TABLE ").append(tableName).append(" RENAME COLUMN ").append(newColumnName).append(" TO ").append(oldColumnName).append(";");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("RENAME TO")) {
            String oldTableName = migrationSql.split("ALTER TABLE")[1].split("RENAME TO")[0].trim();
            String newTableName = migrationSql.split("RENAME TO")[1].trim();
            rollbackSql.append("ALTER TABLE ").append(newTableName).append(" RENAME TO ").append(oldTableName).append(";");
        } else if (migrationSql.contains("CREATE TRIGGER")) {
            String triggerName = migrationSql.split("CREATE TRIGGER")[1].split(" ")[1].trim();
            rollbackSql.append("DROP TRIGGER IF EXISTS ").append(triggerName).append(";");
        } else if (migrationSql.contains("CREATE PROCEDURE") || migrationSql.contains("CREATE FUNCTION")) {
            String procedureName;
            if (migrationSql.contains("CREATE PROCEDURE")) {
                procedureName = migrationSql.split("CREATE PROCEDURE")[1].split(" ")[0].trim();
                rollbackSql.append("DROP PROCEDURE IF EXISTS ").append(procedureName).append(";");
            } else {
                procedureName = migrationSql.split("CREATE FUNCTION")[1].split(" ")[0].trim();
                rollbackSql.append("DROP FUNCTION IF EXISTS ").append(procedureName).append(";");
            }
        } else if (migrationSql.contains("CREATE VIEW")) {
            String viewName = migrationSql.split("CREATE VIEW")[1].split("AS")[0].trim();
            rollbackSql.append("DROP VIEW IF EXISTS ").append(viewName).append(";");
        } else if (migrationSql.contains("CREATE SEQUENCE")) {
            String sequenceName = migrationSql.split("CREATE SEQUENCE")[1].split(" ")[0].trim();
            rollbackSql.append("DROP SEQUENCE IF EXISTS ").append(sequenceName).append(";");
        } else if (migrationSql.contains("CREATE SCHEMA")) {
            String schemaName = migrationSql.split("CREATE SCHEMA")[1].split(" ")[0].trim();
            rollbackSql.append("DROP SCHEMA IF EXISTS ").append(schemaName).append(" CASCADE;");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("ADD CONSTRAINT")) {
            String tableName = extractTableName(migrationSql, "ALTER TABLE");
            String constraintName = migrationSql.split("ADD CONSTRAINT")[1].split(" ")[1].trim();
            rollbackSql.append("ALTER TABLE ").append(tableName).append(" DROP CONSTRAINT IF EXISTS ").append(constraintName).append(";");
        }

        return rollbackSql.toString();
    }

    /**
     * Generates delete conditions from the given migration SQL script.
     *
     * @param migrationSql the migration SQL script
     * @return the generated delete conditions
     */
    private static String generateDeleteConditions(String migrationSql) {
        // Extract the values part of the INSERT statement
        String valuesPart = migrationSql.split("VALUES")[1].trim();
        String[] valueGroups = valuesPart.split("\\),\\s*\\(");

        StringBuilder conditions = new StringBuilder();

        for (int i = 0; i < valueGroups.length; i++) {
            if (i > 0) {
                conditions.append(", ");
            }

            // Add single quotes around values and remove extra characters
            String value = valueGroups[i].replaceAll("[()']", "").trim();

            // Remove trailing semicolon if present
            if (i == valueGroups.length - 1 && value.endsWith(";")) {
                value = value.substring(0, value.length() - 1);
            }

            conditions.append("'").append(value).append("'");
        }

        return conditions.toString();
    }

    /**
     * Extracts the table name from the given SQL script.
     *
     * @param sql the SQL script
     * @param keyword the keyword to split the script by
     * @return the extracted table name
     */
    private static String extractTableName(String sql, String keyword) {
        return sql.split(keyword)[1].split("\\s+")[0].trim();
    }

    /**
     * Extracts the column name from the given SQL script.
     *
     * @param sql the SQL script
     * @param keyword the keyword to split the script by
     * @return the extracted column name
     */
    private static String extractColumnName(String sql, String keyword) {
        return sql.split(keyword)[1].split("\\s+")[1].trim();
    }
}
