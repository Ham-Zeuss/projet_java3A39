package service;

import entite.Notification;
import entite.User;
import util.DataSource;
import entite.Title;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;
    private TitleService titleService;
    private final NotificationService notificationService;

    public UserService() {
        cnx = DataSource.getInstance().getConnection();
        titleService = new TitleService();
        notificationService = new NotificationService();
    }

    @Override
    public void create(User user) {
        // Replaced string concatenation with PreparedStatement to prevent SQL injection
        String requete = "insert into user (current_title_id, nom, prenom, email, roles, password, is_verified, age, gouvernorat, points, numero, enfant_id, photo, status, score_total, is_active, balance, features_unlocked, totp_secret) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            if (user.getCurrentTitle() != null) {
                pst.setInt(1, user.getCurrentTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getRolesAsJson());
            pst.setString(6, user.getPassword());
            pst.setBoolean(7, user.isVerified());
            pst.setInt(8, user.getAge());
            pst.setString(9, user.getGouvernorat());
            if (user.getPoints() != null) {
                pst.setInt(10, user.getPoints());
            } else {
                pst.setNull(10, Types.INTEGER);
            }
            pst.setString(11, user.getNumero());
            if (user.getEnfantId() != null) {
                pst.setInt(12, user.getEnfantId());
            } else {
                pst.setNull(12, Types.INTEGER);
            }
            pst.setString(13, user.getPhoto());
            if (user.getStatus() != null) {
                pst.setString(14, user.getStatus());
            } else {
                pst.setNull(14, Types.VARCHAR);
            }
            if (user.getScoreTotal() != null) {
                pst.setInt(15, user.getScoreTotal());
            } else {
                pst.setNull(15, Types.INTEGER);
            }
            pst.setBoolean(16, user.isActive());
            if (user.getBalance() != null) {
                pst.setDouble(17, user.getBalance());
            } else {
                pst.setNull(17, Types.DECIMAL);
            }
            if (user.getFeaturesUnlocked() != null) {
                pst.setString(18, user.getFeaturesUnlocked());
            } else {
                pst.setNull(18, Types.LONGVARCHAR);
            }
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
    public void createPst(User user) {
        String requete = "insert into user (current_title_id, nom, prenom, email, roles, password, is_verified, age, gouvernorat, points, numero, enfant_id, photo, status, score_total, is_active, balance, features_unlocked, totp_secret) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            if (user.getCurrentTitle() != null) {
                pst.setInt(1, user.getCurrentTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getRolesAsJson());
            pst.setString(6, user.getPassword());
            pst.setBoolean(7, user.isVerified());
            pst.setInt(8, user.getAge());
            pst.setString(9, user.getGouvernorat());
            if (user.getPoints() != null) {
                pst.setInt(10, user.getPoints());
            } else {
                pst.setNull(10, Types.INTEGER);
            }
            pst.setString(11, user.getNumero());
            if (user.getEnfantId() != null) {
                pst.setInt(12, user.getEnfantId());
            } else {
                pst.setNull(12, Types.INTEGER);
            }
            pst.setString(13, user.getPhoto());
            if (user.getStatus() != null) {
                pst.setString(14, user.getStatus());
            } else {
                pst.setNull(14, Types.VARCHAR);
            }
            if (user.getScoreTotal() != null) {
                pst.setInt(15, user.getScoreTotal());
            } else {
                pst.setNull(15, Types.INTEGER);
            }
            pst.setBoolean(16, user.isActive());
            if (user.getBalance() != null) {
                pst.setDouble(17, user.getBalance());
            } else {
                pst.setNull(17, Types.DECIMAL);
            }
            if (user.getFeaturesUnlocked() != null) {
                pst.setString(18, user.getFeaturesUnlocked());
            } else {
                pst.setNull(18, Types.LONGVARCHAR);
            }
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
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {
        String requete = "update user set current_title_id = ?, nom = ?, prenom = ?, email = ?, roles = ?, password = ?, is_verified = ?, age = ?, gouvernorat = ?, points = ?, numero = ?, enfant_id = ?, photo = ?, status = ?, score_total = ?, is_active = ?, balance = ?, features_unlocked = ?, totp_secret = ? where id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            if (user.getCurrentTitle() != null) {
                pst.setInt(1, user.getCurrentTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getRolesAsJson());
            pst.setString(6, user.getPassword());
            pst.setBoolean(7, user.isVerified());
            pst.setInt(8, user.getAge());
            pst.setString(9, user.getGouvernorat());
            if (user.getPoints() != null) {
                pst.setInt(10, user.getPoints());
            } else {
                pst.setNull(10, Types.INTEGER);
            }
            pst.setString(11, user.getNumero());
            if (user.getEnfantId() != null) {
                pst.setInt(12, user.getEnfantId());
            } else {
                pst.setNull(12, Types.INTEGER);
            }
            pst.setString(13, user.getPhoto());
            if (user.getStatus() != null) {
                pst.setString(14, user.getStatus());
            } else {
                pst.setNull(14, Types.VARCHAR);
            }
            if (user.getScoreTotal() != null) {
                pst.setInt(15, user.getScoreTotal());
            } else {
                pst.setNull(15, Types.INTEGER);
            }
            pst.setBoolean(16, user.isActive());
            if (user.getBalance() != null) {
                pst.setDouble(17, user.getBalance());
            } else {
                pst.setNull(17, Types.DECIMAL);
            }
            if (user.getFeaturesUnlocked() != null) {
                pst.setString(18, user.getFeaturesUnlocked());
            } else {
                pst.setNull(18, Types.LONGVARCHAR);
            }
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
        String requete = "select u.*, t.id as title_id, t.name as title_name, t.points_required, t.price from user u left join title t on u.current_title_id = t.id";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(requete)) {
            while (rs.next()) {
                Title title = null;
                if (rs.getObject("title_id") != null) {
                    title = new Title();
                    title.setId(rs.getInt("title_id"));
                    title.setName(rs.getString("title_name"));
                    title.setpoints_required(rs.getInt("points_required"));
                    title.setPrice(rs.getInt("price"));
                }
                String rolesJson = rs.getString("roles");
                List<String> roles = new ArrayList<>();
                // TODO: Parse rolesJson into roles list if needed (e.g., using a JSON library)
                list.add(new User(
                        rs.getInt("id"),
                        title,
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        roles,
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
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public User readById(int id) {
        String requete = "SELECT u.*, t.id as title_id, t.name as title_name, t.points_required, t.price " +
                "FROM user u LEFT JOIN title t ON u.current_title_id = t.id WHERE u.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Title title = null;
                    if (rs.getObject("title_id") != null) {
                        title = new Title();
                        title.setId(rs.getInt("title_id"));
                        title.setName(rs.getString("title_name"));
                        title.setpoints_required(rs.getInt("points_required"));
                        title.setPrice(rs.getInt("price"));
                    }
                    String rolesJson = rs.getString("roles");
                    List<String> roles = new ArrayList<>();
                    // TODO: Parse rolesJson into roles list if needed (e.g., using a JSON library)
                    return new User(
                            rs.getInt("id"),
                            title,
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            roles,
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void updatePoints(int userId, int points) {
        String requete = "UPDATE user SET points = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, points);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCurrentTitleId(int userId, int titleId) {
        String requete = "UPDATE user SET current_title_id = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
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
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Title title = null;
                    if (rs.getObject("title_id") != null) {
                        title = new Title();
                        title.setId(rs.getInt("title_id"));
                        title.setName(rs.getString("title_name"));
                        title.setpoints_required(rs.getInt("points_required"));
                        title.setPrice(rs.getInt("price"));
                    }
                    String rolesJson = rs.getString("roles");
                    List<String> roles = new ArrayList<>();
                    // TODO: Parse rolesJson into roles list if needed
                    User user = new User(
                            rs.getInt("id"),
                            title,
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            roles,
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public User readByIdHamza(int id) {
        String requete = "SELECT u.*, t.id as title_id, t.name as title_name, t.points_required, t.price " +
                "FROM user u LEFT JOIN title t ON u.current_title_id = t.id WHERE u.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Title title = null;
                    if (rs.getObject("title_id") != null) {
                        title = new Title();
                        title.setId(rs.getInt("title_id"));
                        title.setName(rs.getString("title_name"));
                        title.setpoints_required(rs.getInt("points_required"));
                        title.setPrice(rs.getInt("price"));
                    }
                    String rolesJson = rs.getString("roles");
                    List<String> roles = new ArrayList<>();
                    // TODO: Parse rolesJson into roles list if needed
                    return new User(
                            rs.getInt("id"),
                            title,
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            roles,
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, password, roles, is_active FROM user"; // Replaced status with is_active
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return users;
    }

    public List<User> getAllUsersWithStringStatus() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, password, roles, status FROM user";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUseroumaima(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return users;
    }

    public User getUserById(int id) {
        String sql = "SELECT id, nom, prenom, email, password, roles, is_active FROM user WHERE id = ?"; // Replaced status with is_active
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche d'utilisateur par ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT id, nom, prenom, email, password, roles, is_active FROM user WHERE email = ?"; // Replaced status with is_active
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche d'utilisateur par email: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public void addUser(User user) {
        String sql = "INSERT INTO user (nom, prenom, email, password, roles, is_active) VALUES (?, ?, ?, ?, ?, ?)"; // Removed status
        try (PreparedStatement pstmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashPassword(user.getPassword()));
            pstmt.setString(5, user.getRolesAsJson());
            pstmt.setBoolean(6, user.isActive());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("✅ Utilisateur ajouté avec succès: " + user.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Boolean getUserActiveStatus(int userId) {
        String sql = "SELECT is_active FROM user WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_active");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du statut actif: " + e.getMessage());
        }
        return null;
    }

    public void updateUser(User user, boolean isAdmin, String newPassword) {
        Boolean wasActive = getUserActiveStatus(user.getId());
        String sql;
        if (isAdmin && newPassword != null && !newPassword.isEmpty()) {
            sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, password = ?, roles = ?, is_active = ? WHERE id = ?";
        } else {
            sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, roles = ?, is_active = ? WHERE id = ?";
        }
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            if (isAdmin && newPassword != null && !newPassword.isEmpty()) {
                pstmt.setString(4, hashPassword(newPassword));
                pstmt.setString(5, user.getRolesAsJson());
                pstmt.setBoolean(6, user.isActive());
                pstmt.setInt(7, user.getId());
            } else {
                pstmt.setString(4, user.getRolesAsJson());
                pstmt.setBoolean(5, user.isActive());
                pstmt.setInt(6, user.getId());
            }
            pstmt.executeUpdate();

            if (wasActive != null && wasActive && !user.isActive()) {
                Notification notification = new Notification("🔴 L'utilisateur " + user.getEmail() + " a été désactivé.");
                notificationService.create(notification);
            }

            System.out.println("✅ Utilisateur mis à jour avec succès: " + user.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(int id) {
        String deleteCoursSql = "DELETE FROM cours WHERE user_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(deleteCoursSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression des cours associés: " + e.getMessage());
            throw new RuntimeException(e);
        }
        String deleteUserSql = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(deleteUserSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Utilisateur supprimé avec succès. ID: " + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean hasAssociatedCours(int userId) {
        String sql = "SELECT COUNT(*) FROM cours WHERE user_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification des cours associés: " + e.getMessage());
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setActive(rs.getBoolean("is_active")); // Fixed: Use is_active
        String rolesJson = rs.getString("roles");
        user.setRolesFromJson(rolesJson);
        return user;
    }

    private User mapResultSetToUseroumaima(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        String statusValue = rs.getString("status");
        boolean isActive;
        if (statusValue == null || statusValue.isEmpty()) {
            isActive = false;
        } else {
            isActive = statusValue.equalsIgnoreCase("true") || statusValue.equals("1");
        }
        user.setActive(isActive);
        String rolesJson = rs.getString("roles");
        user.setRolesFromJson(rolesJson);
        return user;
    }

    private User mapResultSetToUseroumaima(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        String statusValue = rs.getString("status");
        boolean isActive;
        if (statusValue == null || statusValue.isEmpty()) {
            isActive = false;
        } else {
            isActive = statusValue.equalsIgnoreCase("true") || statusValue.equals("1");
        }
        user.setActive(isActive);
        String rolesJson = rs.getString("roles");
        user.setRolesFromJson(rolesJson);
        return user;
    }

    public boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, password, roles, is_active FROM user WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?"; // Replaced status with is_active
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            String searchParam = "%" + query + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);
            pstmt.setString(3, searchParam);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche d'utilisateurs: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return users;
    }

    public boolean validateUser(User user, boolean isUpdate) {
        if (user.getNom() == null || !user.getNom().matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
            return false;
        }
        if (user.getPrenom() == null || !user.getPrenom().matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
            return false;
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return false;
        }
        if (!isUpdate && (user.getPassword() == null || !user.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"))) {
            return false;
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && confirmPassword != null && password.equals(confirmPassword);
    }

    private String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified;
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRoles().contains("ROLE_ADMIN");
    }
}