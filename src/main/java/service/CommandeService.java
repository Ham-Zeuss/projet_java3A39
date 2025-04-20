package service;

import entite.Commande;
import util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandeService {

    private Connection getConnection() throws SQLException {
        return DataSource.getInstance().getConnection();
    }

    public List<Commande> getAllCommandes() {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande";
        System.out.println("Executing query: " + query);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                commandes.add(new Commande(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("pack_id"),
                        rs.getDouble("amount"),
                        rs.getString("commande_date"),
                        rs.getString("payment_method"),
                        rs.getString("expiry_date"),
                        rs.getString("status")
                ));
                System.out.println("Fetched commande ID: " + rs.getInt("id"));
            }
            System.out.println("Total commandes fetched: " + commandes.size());
        } catch (SQLException e) {
            System.err.println("Error fetching commandes: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return commandes;
    }
}