package service;

import entite.Notification;
import util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final Connection cnx;


    public NotificationService() {
        cnx = DataSource.getInstance().getConnection();
    }

    public void create(Notification notification) {
        String sql = "INSERT INTO notification (message, created_at) VALUES (?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, notification.getMessage());
            pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(notification.getCreatedAt()));
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("✅ Notification ajoutée avec succès: " + notification.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la notification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Notification> readAll() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notification";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setMessage(rs.getString("message"));
                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des notifications: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return notifications;
    }
    public void deleteAll() {
        String sql = "DELETE FROM notification";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("✅ Toutes les notifications ont été supprimées.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression des notifications: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}