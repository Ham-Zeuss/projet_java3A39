package service;

import entite.Commande;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeService {

    private Connection getConnection() throws SQLException {
        return DataSource.getInstance().getConnection();
    }

    /**
     * Fetches all orders from the database.
     */
    public List<Commande> getAllCommandes() {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande";
        System.out.println("Executing query: " + query);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Commande commande = mapResultSetToCommande(rs);
                commandes.add(commande);
                System.out.println("Fetched order: ID=" + commande.getId());
            }
            System.out.println("Total orders fetched: " + commandes.size());
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return commandes;
    }

    /**
     * Fetches a single order by its ID.
     */
    public Commande getCommandeById(int id) {
        String query = "SELECT * FROM commande WHERE id = ?";
        System.out.println("Fetching order with ID: " + id);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToCommande(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order by ID: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return null; // Return null if no order is found
    }

    /**
     * Adds a new order to the database.
     */
    public void addCommande(Commande commande) {
        String query = "INSERT INTO commande (user_id, pack_id, amount, commande_date, payment_method, expiry_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        System.out.println("Adding order: User ID=" + commande.getUserId() + ", Pack ID=" + commande.getPackId());
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, commande.getUserId());
            stmt.setInt(2, commande.getPackId());
            stmt.setDouble(3, commande.getAmount());
            stmt.setString(4, commande.getCommandeDate());
            stmt.setString(5, commande.getPaymentMethod());
            stmt.setString(6, commande.getExpiryDate());
            stmt.setString(7, commande.getStatus());
            int rows = stmt.executeUpdate();
            System.out.println("Order added, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing order in the database.
     */
    public void updateCommande(Commande commande) {
        String query = "UPDATE commande SET user_id = ?, pack_id = ?, amount = ?, commande_date = ?, payment_method = ?, expiry_date = ?, status = ? WHERE id = ?";
        System.out.println("Updating order: ID=" + commande.getId());
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, commande.getUserId());
            stmt.setInt(2, commande.getPackId());
            stmt.setDouble(3, commande.getAmount());
            stmt.setString(4, commande.getCommandeDate());
            stmt.setString(5, commande.getPaymentMethod());
            stmt.setString(6, commande.getExpiryDate());
            stmt.setString(7, commande.getStatus());
            stmt.setInt(8, commande.getId());
            int rows = stmt.executeUpdate();
            System.out.println("Order updated, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an order from the database by its ID.
     */
    public void deleteCommande(int id) {
        String query = "DELETE FROM commande WHERE id = ?";
        System.out.println("Deleting order with ID: " + id);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println("Order deleted, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Maps a ResultSet to a Commande object.
     */
    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        return new Commande(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("pack_id"),
                rs.getDouble("amount"),
                rs.getString("commande_date"),
                rs.getString("payment_method"),
                rs.getString("expiry_date"),
                rs.getString("status")
        );
    }
}