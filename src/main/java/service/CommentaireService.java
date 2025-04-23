package service;

import entite.Commentaire;
import util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    public void create(Commentaire commentaire) throws Exception {
        String query = "INSERT INTO commentaire (user_id, profile_id, comment, consultation_id, report_reason, reported) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, commentaire.getUserId());
            ps.setInt(2, commentaire.getProfileId());
            ps.setString(3, commentaire.getComment());
            ps.setInt(4, commentaire.getConsultationId()); // Required
            ps.setString(5, commentaire.getReportReason());
            ps.setBoolean(6, commentaire.isReported());
            ps.executeUpdate();
        }
    }

    public void delete(Commentaire commentaire) throws Exception {
        String query = "DELETE FROM commentaire WHERE id = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, commentaire.getId());
            ps.executeUpdate();
        }
    }

    public List<Commentaire> readByProfileId(int profileId) throws Exception {
        List<Commentaire> commentaires = new ArrayList<>();
        String query = "SELECT c.* FROM commentaire c WHERE c.profile_id = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire commentaire = new Commentaire();
                    commentaire.setId(rs.getInt("id"));
                    commentaire.setUserId(rs.getInt("user_id"));
                    commentaire.setProfileId(rs.getInt("profile_id"));
                    commentaire.setComment(rs.getString("comment"));
                    commentaire.setConsultationId(rs.getInt("consultation_id"));
                    commentaire.setReportReason(rs.getString("report_reason"));
                    commentaire.setReported(rs.getBoolean("reported"));
                    commentaires.add(commentaire);
                }
            }
        }
        return commentaires;
    }

    public List<Commentaire> readReportedComments() throws Exception {
        List<Commentaire> reportedComments = new ArrayList<>();
        String query = "SELECT * FROM commentaire WHERE reported = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setBoolean(1, true);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire commentaire = new Commentaire();
                    commentaire.setId(rs.getInt("id"));
                    commentaire.setUserId(rs.getInt("user_id"));
                    commentaire.setProfileId(rs.getInt("profile_id"));
                    commentaire.setComment(rs.getString("comment"));
                    commentaire.setConsultationId(rs.getInt("consultation_id"));
                    commentaire.setReportReason(rs.getString("report_reason"));
                    commentaire.setReported(rs.getBoolean("reported"));
                    reportedComments.add(commentaire);
                }
            }
        }
        return reportedComments;
    }

    public void update(Commentaire commentaire) throws Exception {
        String query = "UPDATE commentaire SET report_reason = ?, reported = ? WHERE id = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, commentaire.getReportReason());
            ps.setBoolean(2, commentaire.isReported());
            ps.setInt(3, commentaire.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("No comment found with ID: " + commentaire.getId());
            }
        }
    }

    public int findCompletedConsultationId(int userId, int profileId) throws Exception {
        String query = "SELECT id FROM consultation WHERE user_id = ? AND profile_id = ? AND is_completed = 1 ORDER BY consultation_date DESC LIMIT 1";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return 0; // No completed consultation found
    }
}