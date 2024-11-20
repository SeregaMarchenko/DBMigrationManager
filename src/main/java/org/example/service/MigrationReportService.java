package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.MigrationRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Service class for generating reports on migration operations.
 */
public class MigrationReportService {

    /**
     * Generates a JSON report for the given migration records.
     *
     * @param records the list of migration records
     * @param filePath the file path to save the JSON report
     */
    public void generateJSONReport(List<MigrationRecord> records, String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new FileWriter(filePath), records);
            System.out.println("JSON report generated at " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
