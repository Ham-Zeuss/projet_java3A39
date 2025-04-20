package org.example;

import entite.ResetPasswordRequest;
import entite.User;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.gluonhq.charm.glisten.control.TextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResetPasswordController {

    @FXML
    private TextField newPassword;
    @FXML
    private TextField confirmPassword;
    @FXML
    private Button submitButton;
    @FXML
    private Label errorLabel;

    private String selector;
    private String rawToken;

    private final DataSource dataSource = DataSource.getInstance();

    public void setToken(String token) {
        String[] parts = token.split(":");
        if (parts.length != 2) {
            errorLabel.setText("Token invalide.");
            return;
        }
        this.selector = parts[0];
        this.rawToken = parts[1];
    }

    @FXML
    void submitButtonOnAction(ActionEvent event) {
        if (selector == null || rawToken == null) {
            errorLabel.setText("Token manquant. Veuillez utiliser le lien reçu par email.");
            return;
        }

        String newPass = newPassword.getText();
        String confirmPass = confirmPassword.getText();

        if (newPass == null || newPass.trim().isEmpty() || confirmPass == null || confirmPass.trim().isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        if (!newPass.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            errorLabel.setText("Le mot de passe doit contenir au moins 8 caractères, une lettre, un chiffre et un caractère spécial.");
            return;
        }

        ResetPasswordRequest resetRequest = validateToken(selector, rawToken);
        if (resetRequest != null && !resetRequest.isExpired()) {
            updatePassword(resetRequest.getUser(), newPass);
            deleteResetToken(selector);
            errorLabel.setText("Mot de passe mis à jour avec succès !");
            // Redirect to login.fxml
            try {
                redirectToLogin();
            } catch (Exception e) {
                errorLabel.setText("Erreur lors de la redirection : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Token invalide ou expiré.");
        }
    }

    private ResetPasswordRequest validateToken(String selector, String rawToken) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, user_id, expires_at, selector, hashed_token FROM reset_password_request WHERE selector = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selector);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedToken = rs.getString("hashed_token");
                if (BCrypt.verifyer().verify(rawToken.toCharArray(), hashedToken).verified) {
                    int userId = rs.getInt("user_id");
                    User user = fetchUserById(userId);
                    if (user == null) {
                        return null;
                    }
                    return new ResetPasswordRequest(
                            user,
                            rs.getTimestamp("expires_at").toLocalDateTime(),
                            rs.getString("selector"),
                            hashedToken
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private User fetchUserById(int userId) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, email, password FROM user WHERE id = ?"; // Fixed table name: user (not users)
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updatePassword(User user, String newPassword) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE user SET password = ? WHERE id = ?"; // Fixed table name: user (not users)
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, hashPassword(newPassword));
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du mot de passe : " + e.getMessage());
        }
    }

    private void deleteResetToken(String selector) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "DELETE FROM reset_password_request WHERE selector = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selector);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du token : " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    private void redirectToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}