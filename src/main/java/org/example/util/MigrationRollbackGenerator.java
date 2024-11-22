package org.example.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for generating rollback SQL scripts for migrations.
 */
@Slf4j
public class MigrationRollbackGenerator {

    /**
     * Generates rollback SQL files for the given migration files.
     *
     * @param migrationFolderPath the path to the folder containing migration files
     */
    public static void generateRollbackFiles(String migrationFolderPath){
        try {
            List<String> migrationFiles = Files.list(Paths.get(migrationFolderPath))
                    .filter(path -> path.toString().endsWith(".sql"))
                    .filter(path -> !path.toString().contains("_rollback"))
                    .map(path -> path.toString())
                    .collect(Collectors.toList());

            for (String migrationFile : migrationFiles) {
                String rollbackFile = migrationFile.replace(".sql", "_rollback.sql");
                Path rollbackPath = Path.of(rollbackFile);
                if (!Files.exists(rollbackPath)) {
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
            String tableName = migrationSql.split("CREATE TABLE IF NOT EXISTS")[1].split("\\(")[0].trim();
            rollbackSql.append("DROP TABLE IF EXISTS ").append(tableName).append(" CASCADE;");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("ADD COLUMN")) {
            String tableName = migrationSql.split("ALTER TABLE")[1].split("ADD COLUMN")[0].trim();
            String columnName = migrationSql.split("ADD COLUMN")[1].split(" ")[1].trim();
            rollbackSql.append("ALTER TABLE ").append(tableName).append(" DROP COLUMN IF EXISTS ").append(columnName).append(";");
        } else if (migrationSql.contains("INSERT INTO")) {
            String tableName = migrationSql.split("INSERT INTO")[1].split("\\(")[0].trim();
            String conditions = generateDeleteConditions(migrationSql);
            rollbackSql.append("DELETE FROM ").append(tableName).append(" WHERE ").append(conditions).append(";");
        } else if (migrationSql.contains("UPDATE")) {
            String tableName = migrationSql.split("UPDATE")[1].split("SET")[0].trim();
            String setClause = migrationSql.split("SET")[1].split("WHERE")[0].trim();
            String whereClause = migrationSql.split("WHERE")[1].trim();
            rollbackSql.append("UPDATE ").append(tableName).append(" SET ").append(setClause).append(" WHERE ").append(whereClause).append(";");
        } else if (migrationSql.contains("DELETE FROM")) {
            String tableName = migrationSql.split("DELETE FROM")[1].split("WHERE")[0].trim();
            rollbackSql.append("INSERT INTO ").append(tableName).append(" (SELECT * FROM change_log WHERE table_name = '")
                    .append(tableName).append("' AND operation = 'DELETE' ORDER BY change_time DESC LIMIT 1);");
        } else if (migrationSql.contains("CREATE INDEX")) {
            String indexName = migrationSql.split("CREATE INDEX")[1].split(" ON ")[0].trim();
            rollbackSql.append("DROP INDEX IF EXISTS ").append(indexName).append(";");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("DROP COLUMN")) {
            String tableName = migrationSql.split("ALTER TABLE")[1].split("DROP COLUMN")[0].trim();
            String columnName = migrationSql.split("DROP COLUMN")[1].trim();
            rollbackSql.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN ").append(columnName).append(" TYPE COLUMN_TYPE;");
        } else if (migrationSql.contains("ALTER TABLE") && migrationSql.contains("RENAME COLUMN")) {
            String tableName = migrationSql.split("ALTER TABLE")[1].split("RENAME COLUMN")[0].trim();
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
            String procedureName = migrationSql.split("CREATE PROCEDURE")[1].split(" ")[0].trim();
            rollbackSql.append("DROP PROCEDURE IF EXISTS ").append(procedureName).append(";");
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
            String tableName = migrationSql.split("ALTER TABLE")[1].split("ADD CONSTRAINT")[0].trim();
            String constraintName = migrationSql.split("ADD CONSTRAINT")[1].split(" ")[1].trim();
            rollbackSql.append("ALTER TABLE ").append(tableName).append(" DROP CONSTRAINT IF EXISTS ").append(constraintName).append(";");
        }

        return rollbackSql.toString();
    }

    /**
     * Generates the delete conditions for the given migration SQL script.
     *
     * @param migrationSql the migration SQL script
     * @return the generated delete conditions
     */
    private static String generateDeleteConditions(String migrationSql) {
        String columnsPart = migrationSql.split("\\(")[1].split("\\)")[0].trim();
        String[] columns = columnsPart.split(",");

        String valuesPart = migrationSql.split("VALUES")[1].trim();
        valuesPart = valuesPart.substring(1, valuesPart.length() - 1).trim(); // Remove parentheses
        String[] values = valuesPart.split(",");

        StringBuilder conditions = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            conditions.append(columns[i].trim()).append(" = ").append(values[i].trim());
            if (i < columns.length - 1) {
                conditions.append(" AND ");
            }
        }

        return conditions.toString();
    }
}
