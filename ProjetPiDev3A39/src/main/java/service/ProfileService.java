package service;

import entite.Profile;
import entite.User;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileService implements IService<Profile> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;
    private UserService userService;

    public ProfileService() {
        cnx = DataSource.getInstance().getConnection();
        userService = new UserService();
    }

    // Add this method to find a Profile by user_id
    public Profile findByUserId(int userId) {
        String requete = "select * from profile where user_id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, userId);
            rs = pst.executeQuery();
            if (rs.next()) {
                // Fetch the User object using user_id
                User user = userService.readById(userId);
                // Handle nullable fields
                String biographie = rs.getString("biographie");
                String ressources = rs.getString("ressources");
                Double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
                Double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
                return new Profile(
                        rs.getInt("id"),
                        user,
                        biographie,
                        rs.getString("specialite"),
                        ressources,
                        rs.getDouble("prix_consultation"),
                        latitude,
                        longitude
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void create(Profile profile) {
        String requete = "insert into profile (user_id, biographie, specialite, ressources, prix_consultation, latitude, longitude) " +
                "values(" + profile.getUserId().getId() + ",'" +
                (profile.getBiographie() != null ? profile.getBiographie() : "NULL") + "','" +
                profile.getSpecialite() + "','" +
                (profile.getRessources() != null ? profile.getRessources() : "NULL") + "'," +
                profile.getPrixConsultation() + "," +
                (profile.getLatitude() != null ? profile.getLatitude() : "NULL") + "," +
                (profile.getLongitude() != null ? profile.getLongitude() : "NULL") + ")";
        try {
            ste = cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPst(Profile profile) {
        // Check if a Profile already exists for this user_id
        Profile existingProfile = findByUserId(profile.getUserId().getId());
        if (existingProfile != null) {
            System.out.println("A profile already exists for user_id " + profile.getUserId().getId() + ". Skipping creation.");
            return;
        }

        String requete = "insert into profile (user_id, biographie, specialite, ressources, prix_consultation, latitude, longitude) values (?, ?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            // user_id (not nullable)
            pst.setInt(1, profile.getUserId().getId());
            // biographie (nullable)
            if (profile.getBiographie() != null) {
                pst.setString(2, profile.getBiographie());
            } else {
                pst.setNull(2, Types.LONGVARCHAR);
            }
            pst.setString(3, profile.getSpecialite());
            // ressources (nullable)
            if (profile.getRessources() != null) {
                pst.setString(4, profile.getRessources());
            } else {
                pst.setNull(4, Types.LONGVARCHAR);
            }
            pst.setDouble(5, profile.getPrixConsultation());
            // latitude (nullable)
            if (profile.getLatitude() != null) {
                pst.setDouble(6, profile.getLatitude());
            } else {
                pst.setNull(6, Types.DECIMAL);
            }
            // longitude (nullable)
            if (profile.getLongitude() != null) {
                pst.setDouble(7, profile.getLongitude());
            } else {
                pst.setNull(7, Types.DECIMAL);
            }
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Profile profile) {
        String requete = "delete from profile where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, profile.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Profile profile) {
        String requete = "update profile set user_id = ?, biographie = ?, specialite = ?, ressources = ?, prix_consultation = ?, latitude = ?, longitude = ? where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            // user_id (not nullable)
            pst.setInt(1, profile.getUserId().getId());
            // biographie (nullable)
            if (profile.getBiographie() != null) {
                pst.setString(2, profile.getBiographie());
            } else {
                pst.setNull(2, Types.LONGVARCHAR);
            }
            pst.setString(3, profile.getSpecialite());
            // ressources (nullable)
            if (profile.getRessources() != null) {
                pst.setString(4, profile.getRessources());
            } else {
                pst.setNull(4, Types.LONGVARCHAR);
            }
            pst.setDouble(5, profile.getPrixConsultation());
            // latitude (nullable)
            if (profile.getLatitude() != null) {
                pst.setDouble(6, profile.getLatitude());
            } else {
                pst.setNull(6, Types.DECIMAL);
            }
            // longitude (nullable)
            if (profile.getLongitude() != null) {
                pst.setDouble(7, profile.getLongitude());
            } else {
                pst.setNull(7, Types.DECIMAL);
            }
            pst.setInt(8, profile.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Profile> readAll() {
        List<Profile> list = new ArrayList<>();
        String requete = "select * from profile";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                // Fetch the User object using user_id
                int userId = rs.getInt("user_id");
                User user = userService.readById(userId);
                // Handle nullable fields
                String biographie = rs.getString("biographie");
                String ressources = rs.getString("ressources");
                Double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
                Double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
                list.add(new Profile(
                        rs.getInt("id"),
                        user,
                        biographie,
                        rs.getString("specialite"),
                        ressources,
                        rs.getDouble("prix_consultation"),
                        latitude,
                        longitude
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Profile readById(int id) {
        String requete = "select * from profile where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                // Fetch the User object using user_id
                int userId = rs.getInt("user_id");
                User user = userService.readById(userId);
                // Handle nullable fields
                String biographie = rs.getString("biographie");
                String ressources = rs.getString("ressources");
                Double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
                Double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
                return new Profile(
                        rs.getInt("id"),
                        user,
                        biographie,
                        rs.getString("specialite"),
                        ressources,
                        rs.getDouble("prix_consultation"),
                        latitude,
                        longitude
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}