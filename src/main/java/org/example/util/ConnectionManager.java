package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            log.info("Creating new database connection.");
            connection = DriverManager.getConnection(url, username, password);
        } else {
            log.info("Using existing database connection.");
        }
        return connection;
    }
}
