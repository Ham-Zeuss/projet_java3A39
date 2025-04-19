package service;

import Entity.User;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.DataSource;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final Connection connection;

    public UserService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, password, roles, status FROM user";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }

        return users;
    }

    public User getUserById(int id) {
        String sql = "SELECT id, nom, prenom, email, password, roles, status FROM user WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche d'utilisateur par ID: " + e.getMessage());
        }

        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT id, nom, prenom, email, password, roles, status FROM user WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche d'utilisateur par email: " + e.getMessage());
        }

        return null;
    }

    public void addUser(User user) {
        String sql = "INSERT INTO user (nom, prenom, email, password, roles, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashPassword(user.getPassword()));
            pstmt.setString(5, user.getRolesAsJson());
            pstmt.setString(6, user.getStatus());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("✅ Utilisateur ajouté avec succès: " + user.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
        }
    }

    public void updateUser(User user, boolean isAdmin, String newPassword) {
        String sql;
        if (isAdmin && newPassword != null && !newPassword.isEmpty()) {
            sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, password = ?, roles = ?, status = ? WHERE id = ?";
        } else {
            sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, roles = ?, status = ? WHERE id = ?";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            if (isAdmin && newPassword != null && !newPassword.isEmpty()) {
                pstmt.setString(4, hashPassword(newPassword));
                pstmt.setString(5, user.getRolesAsJson());
                pstmt.setString(6, user.getStatus());
                pstmt.setInt(7, user.getId());
            } else {
                pstmt.setString(4, user.getRolesAsJson());
                pstmt.setString(5, user.getStatus());
                pstmt.setInt(6, user.getId());
            }
            pstmt.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour avec succès: " + user.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }

    public void deleteUser(int id) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Utilisateur supprimé avec succès. ID: " + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password")); // Hashed password
        user.setActive(rs.getBoolean("status"));
        String rolesJson = rs.getString("roles");
        user.setRolesFromJson(rolesJson);

        return user;
    }

    public boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, password, roles, status FROM user WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        }

        return users;
    }

    public boolean validateUser(User user, boolean isUpdate) {
        // Validate nom
        if (user.getNom() == null || !user.getNom().matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
            return false;
        }

        // Validate prenom
        if (user.getPrenom() == null || !user.getPrenom().matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
            return false;
        }

        // Validate email
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return false;
        }

        // Validate password (only for add or admin updates with new password)
        if (!isUpdate && (user.getPassword() == null || !user.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"))) {
            return false;
        }

        // Validate roles
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