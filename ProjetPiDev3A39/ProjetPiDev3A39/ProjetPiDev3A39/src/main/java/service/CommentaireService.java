package service;

import entite.Commentaire;
import entite.User;
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
            ps.setInt(1, commentaire.getUserId().getId());
            ps.setInt(2, commentaire.getProfileId());
            ps.setString(3, commentaire.getComment());
            ps.setInt(4, commentaire.getConsultationId());
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
        String query = "SELECT c.*, u.nom, u.prenom FROM commentaire c LEFT JOIN user u ON c.user_id = u.id WHERE c.profile_id = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire commentaire = new Commentaire();
                    commentaire.setId(rs.getInt("id"));
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    commentaire.setUserId(user);
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
}