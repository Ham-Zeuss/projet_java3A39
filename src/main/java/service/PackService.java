package service;

import entite.Pack;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackService {

    private final Connection conn;

    public PackService() {
        try {
            this.conn = DataSource.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches all packs from the database.
     */
    public List<Pack> getAllPacks() {
        List<Pack> packs = new ArrayList<>();
        String query = "SELECT * FROM pack";
        System.out.println("Executing query: " + query);
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Pack pack = mapResultSetToPack(rs);
                packs.add(pack);
                System.out.println("Fetched pack: " + pack.getName());
            }
            System.out.println("Total packs fetched: " + packs.size());
        } catch (SQLException e) {
            System.err.println("Error fetching packs: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return packs;
    }

    /**
     * Adds a new pack to the database.
     */
    public void addPack(Pack pack) {
        String query = "INSERT INTO pack (price, features, validity_period, name) VALUES (?, ?, ?, ?)";
        System.out.println("Adding pack: " + pack.getName());
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, pack.getPrice());
            stmt.setString(2, pack.getFeatures());
            stmt.setInt(3, pack.getValidityPeriod());
            stmt.setString(4, pack.getName());
            int rows = stmt.executeUpdate();
            System.out.println("Pack added, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error adding pack: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches a single pack by its ID.
     */
    public Pack getPackById(int id) {
        String query = "SELECT * FROM pack WHERE id = ?";
        System.out.println("Fetching pack with ID: " + id);
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPack(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pack by ID: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return null; // Return null if no pack is found
    }

    /**
     * Updates an existing pack in the database.
     */
    public void updatePack(Pack pack) {
        String query = "UPDATE pack SET name = ?, price = ?, features = ?, validity_period = ? WHERE id = ?";
        System.out.println("Updating pack: " + pack.getName());
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, pack.getName());
            stmt.setDouble(2, pack.getPrice());
            stmt.setString(3, pack.getFeatures());
            stmt.setInt(4, pack.getValidityPeriod());
            stmt.setInt(5, pack.getId());
            int rows = stmt.executeUpdate();
            System.out.println("Pack updated, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error updating pack: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a pack from the database by its ID.
     */
    public void deletePack(int id) {
        String query = "DELETE FROM pack WHERE id = ?";
        System.out.println("Deleting pack with ID: " + id);
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println("Pack deleted, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error deleting pack: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Maps a ResultSet to a Pack object.
     */
    private Pack mapResultSetToPack(ResultSet rs) throws SQLException {
        return new Pack(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("features"),
                rs.getInt("validity_period")
        );
    }
}