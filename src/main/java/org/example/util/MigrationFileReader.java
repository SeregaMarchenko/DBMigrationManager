package org.example.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MigrationFileReader {

    public static List<String> getMigrationFiles() throws IOException {
        return Files.list(Paths.get("src/main/resources/db/migrations"))
                .filter(path -> path.toString().endsWith(".sql"))
                .filter(path -> !path.toString().contains("_rollback"))
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    public static String readMigrationFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/main/resources/db/migrations", fileName)));
    }
}
