package service;

import entite.Title;
import entite.User;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection cnx;
    private PreparedStatement pst;
    private ResultSet rs;

    public UserService() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void create(User user) {
        // Implement if needed
    }

    @Override
    public void createPst(User user) {
        // Implement if needed
    }

    @Override
    public void delete(User user) {
        // Implement if needed
    }

    @Override
    public void update(User user) {
        // Implement if needed
    }

    @Override
    public List<User> readAll() {
        // Implement if needed
        return new ArrayList<>();
    }

    @Override
    public User readById(int id) {
        String requete = "SELECT u.*, t.id as title_id, t.name as title_name, t.points_required, t.price " +
                "FROM user u LEFT JOIN title t ON u.current_title_id = t.id WHERE u.id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                Title title = null;
                if (rs.getObject("title_id") != null) {
                    title = new Title();
                    title.setId(rs.getInt("title_id"));
                    title.setName(rs.getString("title_name"));
                    title.setpoints_required(rs.getInt("points_required"));
                    title.setPrice(rs.getInt("price"));
                }
                return new User(
                        rs.getInt("id"),
                        title,
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

    public List<User> getTopUsersByScore(int limit) {
        List<User> users = new ArrayList<>();
        String requete = "SELECT u.*, t.id as title_id, t.name as title_name, t.points_required, t.price " +
                "FROM user u LEFT JOIN title t ON u.current_title_id = t.id " +
                "WHERE u.score_total IS NOT NULL ORDER BY u.score_total DESC LIMIT ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, limit);
            rs = pst.executeQuery();
            while (rs.next()) {
                Title title = null;
                if (rs.getObject("title_id") != null) {
                    title = new Title();
                    title.setId(rs.getInt("title_id"));
                    title.setName(rs.getString("title_name"));
                    title.setpoints_required(rs.getInt("points_required"));
                    title.setPrice(rs.getInt("price"));
                }
                User user = new User(
                        rs.getInt("id"),
                        title,
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
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
}