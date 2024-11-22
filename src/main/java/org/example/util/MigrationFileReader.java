package org.example.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.example.util.paths.MigrationPaths;

/**
 * Utility class for reading migration files from the specified directory.
 */
@Slf4j
public class MigrationFileReader {

    /**
     * Retrieves a list of migration files from the migrations directory.
     *
     * @return a list of migration file names
     */
    public static List<String> getMigrationFiles() {
        try {
            return Files.list(Paths.get(MigrationPaths.MIGRATION_DIRECTORY))
                    .filter(path -> path.toString().endsWith(".sql"))
                    .filter(path -> !path.toString().contains("_rollback"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error reading migration files", e);
            throw new RuntimeException("Critical error while reading migration files", e);
        }
    }

    /**
     * Reads the content of a migration file.
     *
     * @param fileName the name of the migration file
     * @return the content of the migration file as a string
     */
    public static String readMigrationFile(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(MigrationPaths.MIGRATION_DIRECTORY, fileName)));
        } catch (IOException e) {
            log.error("Error reading migration file: " + fileName, e);
            throw new RuntimeException("Critical error while reading migration file: " + fileName, e);
        }
    }
}
