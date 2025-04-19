package service;

import entite.User;
import util.DataSource;

import java.sql.*;

public class UserService {

    private Connection cnx;
    private PreparedStatement pst;
    private ResultSet rs;

    public UserService() {
        cnx = DataSource.getInstance().getConnection();
    }

    public User readById(int id) {
        String requete = "SELECT * FROM user WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        null, // Title set to null
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("roles"),
                        rs.getString("password"),
                        rs.getBoolean("is_verified"),
                        rs.getInt("age"),
                        rs.getString("gouvernorat"),
                        rs.getObject("points", Integer.class),
                        rs.getString("numero"),
                        rs.getObject("enfant_id", Integer.class),
                        rs.getString("photo"),
                        rs.getString("status"),
                        rs.getObject("score_total", Integer.class),
                        rs.getBoolean("is_active"),
                        rs.getObject("balance", Double.class),
                        rs.getString("features_unlocked"),
                        rs.getString("totp_secret")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void updatePoints(int userId, int points) {
        String requete = "UPDATE user SET points = ? WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, points);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCurrentTitleId(int userId, int titleId) {
        String requete = "UPDATE user SET current_title_id = ? WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, titleId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}