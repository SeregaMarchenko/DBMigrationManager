package org.example.service.model;

import java.sql.Timestamp;

public class MigrationRecord {
    private String scriptName;
    private String status;
    private Timestamp appliedAt;

    public MigrationRecord(String scriptName, String status, Timestamp appliedAt) {
        this.scriptName = scriptName;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }
}
