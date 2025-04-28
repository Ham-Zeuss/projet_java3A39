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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResetPasswordController {

    private static final Logger LOGGER = Logger.getLogger(ResetPasswordController.class.getName());

    @FXML
    private PasswordField newPassword;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private Button submitButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Hyperlink backlink;

    private String email;
    private final DataSource dataSource = DataSource.getInstance();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    void submitButtonOnAction(ActionEvent event) {
        if (email == null || email.trim().isEmpty()) {
            errorLabel.setText("Email manquant. Veuillez réessayer depuis la page précédente.");
            return;
        }

        String newPass = newPassword.getText();
        String confirmPass = confirmPassword.getText();

        // Validation des champs
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

        // Obtenir la connexion
        Connection conn = dataSource.getConnection();
        try {
            if (conn == null || conn.isClosed()) {
                LOGGER.severe("Connexion à la base de données fermée avant l'opération.");
                errorLabel.setText("Erreur de connexion à la base de données.");
                return;
            }

            // Validation de la demande
            ResetPasswordRequest resetRequest = validateResetRequest(conn, email);
            if (resetRequest != null && !resetRequest.isExpired()) {
                // Mise à jour du mot de passe
                String hashedPassword = hashPassword(newPass);
                updatePassword(conn, resetRequest.getUser(), hashedPassword);
                deleteResetRequest(conn, email);

                // Afficher l'alerte
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Mot de passe mis à jour avec succès ! Merci.");
                alert.showAndWait();

                // Redirection automatique
                redirectToLogin();
            } else {
                errorLabel.setText("Aucune demande de réinitialisation valide pour cet email.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du mot de passe pour l'email: " + email, e);
            errorLabel.setText("Erreur lors de la mise à jour du mot de passe.");
        } finally {
            // Ne pas fermer la connexion, car elle est gérée par DataSource
        }
    }

    private ResetPasswordRequest validateResetRequest(Connection conn, String email) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id, expires_at FROM reset_password_request WHERE user_id = (SELECT id FROM user WHERE email = ?)")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                LocalDateTime expiresAt = rs.getTimestamp("expires_at").toLocalDateTime();
                User user = fetchUserById(conn, userId);
                if (user != null && LocalDateTime.now().isBefore(expiresAt)) {
                    return new ResetPasswordRequest(user, expiresAt, null, null);
                }
            }
            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la validation de la demande pour l'email: " + email, e);
            return null;
        }
    }

    private User fetchUserById(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id, email, password FROM user WHERE id = ?")) {
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
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'utilisateur ID: " + userId, e);
            return null;
        }
    }

    private void updatePassword(Connection conn, User user, String hashedPassword) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE user SET password = ? WHERE id = ?")) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du mot de passe pour l'utilisateur ID: " + user.getId(), e);
            throw new RuntimeException("Erreur lors de la mise à jour du mot de passe.");
        }
    }

    private void deleteResetRequest(Connection conn, String email) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM reset_password_request WHERE user_id = (SELECT id FROM user WHERE email = ?)")) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la demande pour l'email: " + email, e);
            throw new RuntimeException("Erreur lors de la suppression de la demande.");
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
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
        LOGGER.info("Redirection réussie vers login.fxml");
    }

    @FXML
    void backlogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/login.fxml"));
            Stage stage = (Stage) backlink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setResizable(false);
            stage.show();
            LOGGER.info("Redirection réussie vers login.fxml via backlogin");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la redirection vers login.fxml", e);
            errorLabel.setText("Erreur lors de la redirection.");
        }
    }
}