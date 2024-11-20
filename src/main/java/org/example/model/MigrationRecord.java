package org.example.model;

import java.sql.Timestamp;

/**
 * Model class representing a record of a database migration.
 */
public class MigrationRecord {
    private String scriptName;
    private String status;
    private Timestamp appliedAt;

    /**
     * Constructs a new MigrationRecord with the specified script name, status, and applied timestamp.
     *
     * @param scriptName the name of the migration script
     * @param status the status of the migration (e.g., SUCCESS, FAILED)
     * @param appliedAt the timestamp when the migration was applied
     */
    public MigrationRecord(String scriptName, String status, Timestamp appliedAt) {
        this.scriptName = scriptName;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    /**
     * Gets the name of the migration script.
     *
     * @return the name of the migration script
     */
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Sets the name of the migration script.
     *
     * @param scriptName the name of the migration script
     */
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    /**
     * Gets the status of the migration.
     *
     * @return the status of the migration
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the migration.
     *
     * @param status the status of the migration
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the timestamp when the migration was applied.
     *
     * @return the timestamp when the migration was applied
     */
    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    /**
     * Sets the timestamp when the migration was applied.
     *
     * @param appliedAt the timestamp when the migration was applied
     */
    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }
}
