package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private String url = "jdbc:mysql://localhost:3306/main?useSSL=false";
    private String username = "root";
    private String password = "";
    private Connection connection;
    private static DataSource instance;

    private DataSource() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            throw new RuntimeException("Failed to load JDBC driver", e);
        } catch (SQLException e) {
            System.err.println("Connection failed to " + url + ": " + e.getMessage());
            // Fallback to 172.20.10.7
            url = "jdbc:mysql://172.20.10.7:3306/main?useSSL=false";
            try {
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Fallback connection to 172.20.10.7 established successfully!");
            } catch (SQLException ex) {
                System.err.println("Fallback connection failed: " + ex.getMessage());
                throw new RuntimeException("Failed to create database connection", ex);
            }
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            synchronized (DataSource.class) {
                if (instance == null) {
                    instance = new DataSource();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Re-established connection to " + url);
            }
        } catch (SQLException e) {
            System.err.println("Failed to re-establish connection: " + e.getMessage());
            throw new RuntimeException("Connection error", e);
        }
        return connection;
    }
}