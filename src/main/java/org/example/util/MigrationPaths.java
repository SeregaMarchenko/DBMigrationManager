package org.example.util;

/**
 * Utility class for storing migration file paths as constants.
 */
public class MigrationPaths {

    public static final String MIGRATION_DIRECTORY = PropertiesUtils.getProperty("migration.directory");
    public static final String ROLLBACK_DIRECTORY = PropertiesUtils.getProperty("rollback.directory");

}
