package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private static String url = PropertiesUtils.getProperty("db.url");
    private static String username = PropertiesUtils.getProperty("db.username");
    private static String password = PropertiesUtils.getProperty("db.password");
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
}
