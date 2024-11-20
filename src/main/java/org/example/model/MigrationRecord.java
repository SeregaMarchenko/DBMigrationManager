package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Model class representing a record of a database migration.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationRecord {
    private String scriptName;
    private String status;
    private Timestamp appliedAt;
}
