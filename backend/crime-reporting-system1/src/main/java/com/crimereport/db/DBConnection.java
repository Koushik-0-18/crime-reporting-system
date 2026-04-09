package com.crimereport.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        String url = getRequiredEnv("DB_URL");
        String user = getRequiredEnv("DB_USER");
        String password = getRequiredEnv("DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    private static String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
}
