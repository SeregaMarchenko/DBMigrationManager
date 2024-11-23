package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.example.model.MigrationRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for generating migration reports in JSON format.
 */
@Slf4j
public class MigrationReportService {
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Generates a JSON report for the given list of migration records.
     * This method reads existing records from the file, appends new migration records,
     * and writes the updated list back to the file in JSON format.
     *
     * @param migrationRecords the list of migration records
     * @param filePath the file path to save the JSON report
     */
    public void generateJSONReport(List<MigrationRecord> migrationRecords, String filePath) {
        try {
            // Read existing records
            List<MigrationRecord> existingRecords = new ArrayList<>();
            File file = new File(filePath);
            if (file.exists()) {
                CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, MigrationRecord.class);
                existingRecords = objectMapper.readValue(file, listType);
            }

            // Append new records
            existingRecords.addAll(migrationRecords);

            // Write updated records
            objectMapper.writeValue(file, existingRecords);
            log.info("Migration report updated at {}", filePath);
        } catch (IOException e) {
            log.error("Failed to generate migration report", e);
            throw new RuntimeException("Critical error while generating migration report", e);
        }
    }
}
