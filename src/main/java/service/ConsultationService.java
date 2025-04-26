package service;

import entite.Consultation;
import entite.Profile;
import entite.User;
import util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultationService {

    public void create(Consultation consultation) throws Exception {
        String query = "INSERT INTO consultation (user_id, profile_id, consultation_date, is_completed) VALUES (?, ?, ?, ?)";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, consultation.getUserId().getId());
            ps.setInt(2, consultation.getProfileId().getId());
            ps.setTimestamp(3, Timestamp.valueOf(consultation.getConsultationDate()));
            ps.setBoolean(4, consultation.isCompleted());
            ps.executeUpdate();
        }
    }

    public boolean checkForConflict(int profileId, LocalDateTime consultationDateTime) throws Exception {
        String query = "SELECT COUNT(*) FROM consultation WHERE profile_id = ? AND consultation_date = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, profileId);
            ps.setTimestamp(2, Timestamp.valueOf(consultationDateTime));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Consultation> readAll() throws Exception {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.id, c.user_id, c.profile_id, c.consultation_date, c.is_completed, " +
                "u.nom AS user_nom, u.prenom AS user_prenom, " +
                "p.specialite, p.user_id AS profile_user_id, u2.nom AS profile_nom, u2.prenom AS profile_prenom " +
                "FROM consultation c " +
                "JOIN user u ON c.user_id = u.id " +
                "JOIN profile p ON c.profile_id = p.id " +
                "JOIN user u2 ON p.user_id = u2.id";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Consultation consultation = new Consultation();
                consultation.setId(rs.getInt("id"));

                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setNom(rs.getString("user_nom"));
                user.setPrenom(rs.getString("user_prenom"));
                consultation.setUserId(user);

                Profile profile = new Profile();
                profile.setId(rs.getInt("profile_id"));
                profile.setSpecialite(rs.getString("specialite"));
                User profileUser = new User();
                profileUser.setId(rs.getInt("profile_user_id"));
                profileUser.setNom(rs.getString("profile_nom"));
                profileUser.setPrenom(rs.getString("profile_prenom"));
                profile.setUserId(profileUser);
                consultation.setProfileId(profile);

                consultation.setConsultationDate(rs.getTimestamp("consultation_date").toLocalDateTime());
                consultation.setCompleted(rs.getBoolean("is_completed"));

                consultations.add(consultation);
            }
        }
        return consultations;
    }

    public void delete(Consultation consultation) throws Exception {
        String query = "DELETE FROM consultation WHERE id = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, consultation.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("No consultation found with ID " + consultation.getId());
            }
        }
    }

    public void update(Consultation consultation) throws Exception {
        String query = "UPDATE consultation SET user_id = ?, profile_id = ?, consultation_date = ?, is_completed = ? WHERE id = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, consultation.getUserId().getId());
            ps.setInt(2, consultation.getProfileId().getId());
            ps.setTimestamp(3, Timestamp.valueOf(consultation.getConsultationDate()));
            ps.setBoolean(4, consultation.isCompleted());
            ps.setInt(5, consultation.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("No consultation found with ID " + consultation.getId());
            }
        }
    }

    public List<Consultation> readByUserId(int userId) throws Exception {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.id, c.user_id, c.profile_id, c.consultation_date, c.is_completed, " +
                "u.nom AS user_nom, u.prenom AS user_prenom, " +
                "p.specialite, p.user_id AS profile_user_id, u2.nom AS profile_nom, u2.prenom AS profile_prenom " +
                "FROM consultation c " +
                "JOIN user u ON c.user_id = u.id " +
                "JOIN profile p ON c.profile_id = p.id " +
                "JOIN user u2 ON p.user_id = u2.id " +
                "WHERE c.user_id = ?";
        Connection conn = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Consultation consultation = new Consultation();
                    consultation.setId(rs.getInt("id"));

                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setNom(rs.getString("user_nom"));
                    user.setPrenom(rs.getString("user_prenom"));
                    consultation.setUserId(user);

                    Profile profile = new Profile();
                    profile.setId(rs.getInt("profile_id"));
                    profile.setSpecialite(rs.getString("specialite"));
                    User profileUser = new User();
                    profileUser.setId(rs.getInt("profile_user_id"));
                    profileUser.setNom(rs.getString("profile_nom"));
                    profileUser.setPrenom(rs.getString("profile_prenom"));
                    profile.setUserId(profileUser);
                    consultation.setProfileId(profile);

                    consultation.setConsultationDate(rs.getTimestamp("consultation_date").toLocalDateTime());
                    consultation.setCompleted(rs.getBoolean("is_completed"));

                    consultations.add(consultation);
                }
            }
        }
        return consultations;
    }
}