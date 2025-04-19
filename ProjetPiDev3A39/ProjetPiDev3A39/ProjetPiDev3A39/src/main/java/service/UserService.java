package service;

import entite.User;
import entite.Title;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;
    private TitleService titleService; // To fetch Title objects

    public UserService() {
        cnx = DataSource.getInstance().getConnection();
        titleService = new TitleService();
    }

    @Override
    public void create(User user) {
        String requete = "insert into user (current_title_id, nom, prenom, email, roles, password, is_verified, age, gouvernorat, points, numero, enfant_id, photo, status, score_total, is_active, balance, features_unlocked, totp_secret) " +
                "values(" + (user.getCurrentTitle() != null ? user.getCurrentTitle().getId() : "NULL") + ",'" +
                user.getNom() + "','" +
                user.getPrenom() + "','" +
                user.getEmail() + "','" +
                user.getRoles() + "','" +
                user.getPassword() + "'," +
                (user.isVerified() ? 1 : 0) + "," +
                user.getAge() + ",'" +
                user.getGouvernorat() + "'," +
                (user.getPoints() != null ? user.getPoints() : "NULL") + ",'" +
                user.getNumero() + "'," +
                (user.getEnfantId() != null ? user.getEnfantId() : "NULL") + ",'" +
                user.getPhoto() + "','" +
                (user.getStatus() != null ? user.getStatus() : "NULL") + "'," +
                (user.getScoreTotal() != null ? user.getScoreTotal() : "NULL") + "," +
                (user.isActive() ? 1 : 0) + "," +
                (user.getBalance() != null ? user.getBalance() : "NULL") + ",'" +
                (user.getFeaturesUnlocked() != null ? user.getFeaturesUnlocked() : "NULL") + "','" +
                (user.getTotpSecret() != null ? user.getTotpSecret() : "NULL") + "')";
        try {
            ste = cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createPst(User user) {
        String requete = "insert into user (current_title_id, nom, prenom, email, roles, password, is_verified, age, gouvernorat, points, numero, enfant_id, photo, status, score_total, is_active, balance, features_unlocked, totp_secret) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            // current_title_id (nullable)
            if (user.getCurrentTitle() != null) {
                pst.setInt(1, user.getCurrentTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getRoles());
            pst.setString(6, user.getPassword());
            pst.setBoolean(7, user.isVerified());
            pst.setInt(8, user.getAge());
            pst.setString(9, user.getGouvernorat());
            // points (nullable)
            if (user.getPoints() != null) {
                pst.setInt(10, user.getPoints());
            } else {
                pst.setNull(10, Types.INTEGER);
            }
            pst.setString(11, user.getNumero());
            // enfant_id (nullable)
            if (user.getEnfantId() != null) {
                pst.setInt(12, user.getEnfantId());
            } else {
                pst.setNull(12, Types.INTEGER);
            }
            pst.setString(13, user.getPhoto());
            // status (nullable)
            if (user.getStatus() != null) {
                pst.setString(14, user.getStatus());
            } else {
                pst.setNull(14, Types.VARCHAR);
            }
            // score_total (nullable)
            if (user.getScoreTotal() != null) {
                pst.setInt(15, user.getScoreTotal());
            } else {
                pst.setNull(15, Types.INTEGER);
            }
            pst.setBoolean(16, user.isActive());
            // balance (nullable)
            if (user.getBalance() != null) {
                pst.setDouble(17, user.getBalance());
            } else {
                pst.setNull(17, Types.DECIMAL);
            }
            // features_unlocked (nullable)
            if (user.getFeaturesUnlocked() != null) {
                pst.setString(18, user.getFeaturesUnlocked());
            } else {
                pst.setNull(18, Types.LONGVARCHAR);
            }
            // totp_secret (nullable)
            if (user.getTotpSecret() != null) {
                pst.setString(19, user.getTotpSecret());
            } else {
                pst.setNull(19, Types.VARCHAR);
            }
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(User user) {
        String requete = "delete from user where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {
        String requete = "update user set current_title_id = ?, nom = ?, prenom = ?, email = ?, roles = ?, password = ?, is_verified = ?, age = ?, gouvernorat = ?, points = ?, numero = ?, enfant_id = ?, photo = ?, status = ?, score_total = ?, is_active = ?, balance = ?, features_unlocked = ?, totp_secret = ? where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            // current_title_id (nullable)
            if (user.getCurrentTitle() != null) {
                pst.setInt(1, user.getCurrentTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getRoles());
            pst.setString(6, user.getPassword());
            pst.setBoolean(7, user.isVerified());
            pst.setInt(8, user.getAge());
            pst.setString(9, user.getGouvernorat());
            // points (nullable)
            if (user.getPoints() != null) {
                pst.setInt(10, user.getPoints());
            } else {
                pst.setNull(10, Types.INTEGER);
            }
            pst.setString(11, user.getNumero());
            // enfant_id (nullable)
            if (user.getEnfantId() != null) {
                pst.setInt(12, user.getEnfantId());
            } else {
                pst.setNull(12, Types.INTEGER);
            }
            pst.setString(13, user.getPhoto());
            // status (nullable)
            if (user.getStatus() != null) {
                pst.setString(14, user.getStatus());
            } else {
                pst.setNull(14, Types.VARCHAR);
            }
            // score_total (nullable)
            if (user.getScoreTotal() != null) {
                pst.setInt(15, user.getScoreTotal());
            } else {
                pst.setNull(15, Types.INTEGER);
            }
            pst.setBoolean(16, user.isActive());
            // balance (nullable)
            if (user.getBalance() != null) {
                pst.setDouble(17, user.getBalance());
            } else {
                pst.setNull(17, Types.DECIMAL);
            }
            // features_unlocked (nullable)
            if (user.getFeaturesUnlocked() != null) {
                pst.setString(18, user.getFeaturesUnlocked());
            } else {
                pst.setNull(18, Types.LONGVARCHAR);
            }
            // totp_secret (nullable)
            if (user.getTotpSecret() != null) {
                pst.setString(19, user.getTotpSecret());
            } else {
                pst.setNull(19, Types.VARCHAR);
            }
            pst.setInt(20, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> readAll() {
        List<User> list = new ArrayList<>();
        String requete = "select * from user";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                // Fetch the Title object using current_title_id
                Integer titleId = rs.getObject("current_title_id") != null ? rs.getInt("current_title_id") : null;
                Title title = titleId != null ? titleService.readById(titleId) : null;
                // Handle nullable fields
                Integer points = rs.getObject("points") != null ? rs.getInt("points") : null;
                Integer enfantId = rs.getObject("enfant_id") != null ? rs.getInt("enfant_id") : null;
                String status = rs.getString("status");
                Integer scoreTotal = rs.getObject("score_total") != null ? rs.getInt("score_total") : null;
                Double balance = rs.getObject("balance") != null ? rs.getDouble("balance") : null;
                String featuresUnlocked = rs.getString("features_unlocked");
                String totpSecret = rs.getString("totp_secret");
                list.add(new User(
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
                        points,
                        rs.getString("numero"),
                        enfantId,
                        rs.getString("photo"),
                        status,
                        scoreTotal,
                        rs.getBoolean("is_active"),
                        balance,
                        featuresUnlocked,
                        totpSecret
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public User readById(int id) {
        String requete = "select * from user where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                // Fetch the Title object using current_title_id
                Integer titleId = rs.getObject("current_title_id") != null ? rs.getInt("current_title_id") : null;
                Title title = titleId != null ? titleService.readById(titleId) : null;
                // Handle nullable fields
                Integer points = rs.getObject("points") != null ? rs.getInt("points") : null;
                Integer enfantId = rs.getObject("enfant_id") != null ? rs.getInt("enfant_id") : null;
                String status = rs.getString("status");
                Integer scoreTotal = rs.getObject("score_total") != null ? rs.getInt("score_total") : null;
                Double balance = rs.getObject("balance") != null ? rs.getDouble("balance") : null;
                String featuresUnlocked = rs.getString("features_unlocked");
                String totpSecret = rs.getString("totp_secret");
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
                        points,
                        rs.getString("numero"),
                        enfantId,
                        rs.getString("photo"),
                        status,
                        scoreTotal,
                        rs.getBoolean("is_active"),
                        balance,
                        featuresUnlocked,
                        totpSecret
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}