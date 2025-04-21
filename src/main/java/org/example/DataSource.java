package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static final String URL = "jdbc:mysql://localhost:3306/main";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Instance unique de la connexion
    private static DataSource instance;
    private Connection conn;

    // Constructeur privé pour empêcher l'instanciation directe
    private DataSource() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Chargement explicite du driver
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion réussie à la base de données !");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver JDBC introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion à la base de données : " + e.getMessage());
        }
    }

    // Méthode pour obtenir l'instance unique
    public static DataSource getInstance() {
        if (instance == null) {
            synchronized (DataSource.class) { // 🔒 Synchronisation pour éviter les problèmes en environnement multithread
                if (instance == null) {
                    instance = new DataSource();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }
}
