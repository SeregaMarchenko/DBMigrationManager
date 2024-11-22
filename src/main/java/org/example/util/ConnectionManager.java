package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for managing database connections.
 */
@Slf4j
public class ConnectionManager {
    private static String url = PropertiesUtils.getProperty("db.url");
    private static String username = PropertiesUtils.getProperty("db.username");
    private static String password = PropertiesUtils.getProperty("db.password");
    private static Connection connection;

    /**
     * Retrieves a connection to the database.
     *
     * @return the database connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                log.info("Creating new database connection.");
                connection = DriverManager.getConnection(url, username, password);
            } else {
                log.info("Using existing database connection.");
            }
        } catch (SQLException e) {
            log.error("Failed to create database connection", e);
            throw new RuntimeException("Critical error while creating database connection", e);
        }
        return connection;
    }
}
