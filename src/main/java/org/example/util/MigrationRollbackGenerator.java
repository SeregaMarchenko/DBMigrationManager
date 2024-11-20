package org.example.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MigrationRollbackGenerator {

    public static void generateRollbackFiles(String migrationFolderPath) throws IOException {
        List<String> migrationFiles = getSqlFiles(migrationFolderPath);

        for (String file : migrationFiles) {
            String migrationSql = new String(Files.readAllBytes(Paths.get(migrationFolderPath, file)));
            String rollbackSql = generateRollbackSql(migrationSql);
            String rollbackFileName = file.replace(".sql", "_rollback.sql");
            Files.write(Paths.get(migrationFolderPath, rollbackFileName), rollbackSql.getBytes());
        }
    }

    private static List<String> getSqlFiles(String folderPath) throws IOException {
        return Files.list(Paths.get(folderPath))
                .filter(path -> path.toString().endsWith(".sql"))
                .filter(path -> !path.toString().contains("_rollback"))
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
    }

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
        }
        return rollbackSql.toString();
    }

    private static String generateDeleteConditions(String migrationSql) {
        // Extract table name
        String tableName = migrationSql.split("INSERT INTO")[1].split("\\(")[0].trim();

        // Extract column names
        String columnsPart = migrationSql.split("\\(")[1].split("\\)")[0].trim();
        String[] columns = columnsPart.split(",");

        // Extract values
        String valuesPart = migrationSql.split("VALUES")[1].trim();
        valuesPart = valuesPart.substring(1, valuesPart.length() - 1).trim(); // Remove parentheses
        String[] values = valuesPart.split(",");

        // Generate delete conditions
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