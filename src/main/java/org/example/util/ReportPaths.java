package org.example.util;

/**
 * Utility class for storing report file paths as constants.
 */
public class ReportPaths {

    public static final String MIGRATE_REPORT_DIRECTORY = PropertiesUtils.getProperty("report.migrate.directory");
    public static final String ROLLBACK_REPORT_DIRECTORY = PropertiesUtils.getProperty("report.rollback.directory");

}
