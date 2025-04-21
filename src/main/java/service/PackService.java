package service;

import entite.Pack;
import util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PackService {

    private Connection getConnection() throws SQLException {
        return DataSource.getInstance().getConnection();
    }

    public List<Pack> getAllPacks() {
        List<Pack> packs = new ArrayList<>();
        String query = "SELECT * FROM pack";
        System.out.println("Executing query: " + query);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Pack pack = new Pack(
                        rs.getInt("id"),
                        rs.getDouble("price"),
                        rs.getString("features"),
                        rs.getInt("validity_period"),
                        rs.getString("name")
                );
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

    public void addPack(Pack pack) {
        String query = "INSERT INTO pack (price, features, validity_period, name) VALUES (?, ?, ?, ?)";
        System.out.println("Adding pack: " + pack.getName());
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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

    public void updatePack(Pack pack) {
        String query = "UPDATE pack SET price = ?, features = ?, validity_period = ?, name = ? WHERE id = ?";
        System.out.println("Updating pack ID: " + pack.getId());
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, pack.getPrice());
            stmt.setString(2, pack.getFeatures());
            stmt.setInt(3, pack.getValidityPeriod());
            stmt.setString(4, pack.getName());
            stmt.setInt(5, pack.getId());
            int rows = stmt.executeUpdate();
            System.out.println("Pack updated, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error updating pack: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public void deletePack(int id) {
        String query = "DELETE FROM pack WHERE id = ?";
        System.out.println("Deleting pack ID: " + id);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println("Pack deleted, rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println("Error deleting pack: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}