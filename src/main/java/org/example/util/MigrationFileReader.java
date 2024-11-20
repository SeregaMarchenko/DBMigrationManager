package org.example.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for reading migration files from the specified directory.
 */
public class MigrationFileReader {

    /**
     * Retrieves a list of migration files from the migrations directory.
     *
     * @return a list of migration file names
     * @throws IOException if an I/O error occurs
     */
    public static List<String> getMigrationFiles() throws IOException {
        return Files.list(Paths.get(MigrationPaths.MIGRATION_DIRECTORY))
                .filter(path -> path.toString().endsWith(".sql"))
                .filter(path -> !path.toString().contains("_rollback"))
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Reads the content of a migration file.
     *
     * @param fileName the name of the migration file
     * @return the content of the migration file as a string
     * @throws IOException if an I/O error occurs
     */
    public static String readMigrationFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(MigrationPaths.MIGRATION_DIRECTORY, fileName)));
    }
}
