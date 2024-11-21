package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.model.MigrationRecord;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service class for generating migration reports in JSON format.
 */
@Slf4j
public class MigrationReportService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates a JSON report for the given list of migration records.
     *
     * @param migrationRecords the list of migration records
     * @param filePath the file path to save the JSON report
     * @throws IOException if an I/O error occurs
     */
    public void generateJSONReport(List<MigrationRecord> migrationRecords, String filePath) throws IOException {
        try {
            objectMapper.writeValue(new File(filePath), migrationRecords);
            log.info("Migration report generated at {}", filePath);
        } catch (IOException e) {
            log.error("Failed to generate migration report", e);
            throw e;
        }
    }
}
