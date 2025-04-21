package service;

import entite.Consultation;
import entite.User;
import entite.Profile;
import util.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultationService implements IService<Consultation> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;
    private UserService userService;
    private ProfileService profileService;

    public ConsultationService() {
        cnx = DataSource.getInstance().getConnection();
        userService = new UserService();
        profileService = new ProfileService();
    }

    @Override
    public void create(Consultation consultation) {
        String requete = "insert into consultation (user_id, profile_id, consultation_date, is_completed) " +
                "values(" + consultation.getUserId().getId() + "," +
                consultation.getProfileId().getId() + ",'" +
                Timestamp.valueOf(consultation.getConsultationDate()) + "'," +
                (consultation.isCompleted() ? 1 : 0) + ")";
        try {
            ste = cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPst(Consultation consultation) {
        String requete = "insert into consultation (user_id, profile_id, consultation_date, is_completed) values (?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, consultation.getUserId().getId());
            pst.setInt(2, consultation.getProfileId().getId());
            pst.setTimestamp(3, Timestamp.valueOf(consultation.getConsultationDate()));
            pst.setBoolean(4, consultation.isCompleted());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Consultation consultation) {
        String requete = "delete from consultation where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, consultation.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Consultation consultation) {
        String requete = "update consultation set user_id = ?, profile_id = ?, consultation_date = ?, is_completed = ? where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, consultation.getUserId().getId());
            pst.setInt(2, consultation.getProfileId().getId());
            pst.setTimestamp(3, Timestamp.valueOf(consultation.getConsultationDate()));
            pst.setBoolean(4, consultation.isCompleted());
            pst.setInt(5, consultation.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Consultation> readAll() {
        List<Consultation> list = new ArrayList<>();
        String requete = "select * from consultation";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                User user = userService.readById(userId);
                int profileId = rs.getInt("profile_id");
                Profile profile = profileService.readById(profileId);
                Timestamp timestamp = rs.getTimestamp("consultation_date");
                LocalDateTime consultationDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                list.add(new Consultation(
                        rs.getInt("id"),
                        user,
                        profile,
                        consultationDate,
                        rs.getBoolean("is_completed")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Consultation readById(int id) {
        String requete = "select * from consultation where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                User user = userService.readById(userId);
                int profileId = rs.getInt("profile_id");
                Profile profile = profileService.readById(profileId);
                Timestamp timestamp = rs.getTimestamp("consultation_date");
                LocalDateTime consultationDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                return new Consultation(
                        rs.getInt("id"),
                        user,
                        profile,
                        consultationDate,
                        rs.getBoolean("is_completed")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Consultation> readByUserId(int userId) {
        List<Consultation> list = new ArrayList<>();
        String requete = "select * from consultation where user_id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, userId);
            rs = pst.executeQuery();
            while (rs.next()) {
                User user = userService.readById(rs.getInt("user_id"));
                Profile profile = profileService.readById(rs.getInt("profile_id"));
                Timestamp timestamp = rs.getTimestamp("consultation_date");
                LocalDateTime consultationDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                list.add(new Consultation(
                        rs.getInt("id"),
                        user,
                        profile,
                        consultationDate,
                        rs.getBoolean("is_completed")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // Method to check for scheduling conflicts
    public boolean checkForConflict(int profileId, LocalDateTime consultationDate) {
        String requete = "select * from consultation where profile_id = ? and consultation_date = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, profileId);
            pst.setTimestamp(2, Timestamp.valueOf(consultationDate));
            rs = pst.executeQuery();
            return rs.next(); // Returns true if a consultation exists at this date and time for the profile
        } catch (SQLException e) {
            throw new RuntimeException("Error checking for scheduling conflict: " + e.getMessage());
        }
    }
}