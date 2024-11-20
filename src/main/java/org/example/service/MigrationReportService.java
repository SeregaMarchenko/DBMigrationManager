package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.example.service.model.MigrationRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MigrationReportService {

    public void generateCSVReport(List<MigrationRecord> records, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(new String[]{"Script Name", "Status", "Timestamp"});

            for (MigrationRecord record : records) {
                writer.writeNext(new String[]{
                        record.getScriptName(),
                        record.getStatus(),
                        record.getAppliedAt().toString()
                });
            }

            System.out.println("CSV report generated at " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
