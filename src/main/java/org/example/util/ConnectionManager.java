package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections.
 */
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
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
}
