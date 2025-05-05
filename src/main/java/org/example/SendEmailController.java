package org.example;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.gluonhq.charm.glisten.control.TextField;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendEmailController {

    private static final Logger LOGGER = Logger.getLogger(SendEmailController.class.getName());

    @FXML
    private TextField sendTO;
    @FXML
    private Label connAlerte;

    private final DataSource dataSource = DataSource.getInstance();

    @FXML
    void sendBtnOnAction(ActionEvent event) {
        String email = sendTO.getText();

        // Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            connAlerte.setText("Veuillez entrer un email.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            connAlerte.setText("Veuillez entrer un email valide.");
            return;
        }

        // Obtenir la connexion
        Connection conn = dataSource.getConnection();
        try {
            // Vérifier si la connexion est ouverte
            if (conn == null || conn.isClosed()) {
                LOGGER.severe("Connexion à la base de données fermée ou nulle avant l'opération.");
                connAlerte.setText("Erreur de connexion à la base de données.");
                return;
            }

            // Vérifier l'existence de l'utilisateur
            if (!userExistsByEmail(conn, email)) {
                connAlerte.setText("Aucun utilisateur trouvé avec cet email.");
                return;
            }

            // Création et stockage du jeton
            String selector = UUID.randomUUID().toString().substring(0, 20);
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = BCrypt.withDefaults().hashToString(12, rawToken.toCharArray());
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO reset_password_request (user_id, selector, hashed_token, expires_at) VALUES ((SELECT id FROM user WHERE email = ?), ?, ?, ?)")) {
                stmt.setString(1, email);
                stmt.setString(2, selector);
                stmt.setString(3, hashedToken);
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(expiryDate));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la demande de réinitialisation pour l'email: " + email, e);
            connAlerte.setText("Erreur lors de la création de la demande.");
            return;
        } finally {
            // Ne pas fermer la connexion, car elle est gérée par DataSource
        }

        // Envoi de l'email et redirection
        try {
            sendEmail(email);
            resetAccountFormreset(event, email);
        } catch (MessagingException | IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email pour: " + email, e);
            connAlerte.setText("Erreur lors de l'envoi de l'email.");
        }
    }

    private boolean userExistsByEmail(Connection conn, String email) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM user WHERE email = ?")) {
            pstmt.setString(1, email);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification de l'email: " + email, e);
            return false;
        }
    }

    private void sendEmail(String email) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("628d2c7935a2da", "4d1baf0d355e70");
            }
        });

        String htmlContent;
        try (var inputStream = getClass().getResourceAsStream("/User/email_template.html")) {
            if (inputStream == null) {
                throw new IOException("Template email introuvable.");
            }
            htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@example.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("Annonce de réinitialisation de mot de passe");
        message.setContent(htmlContent, "text/html; charset=UTF-8");
        Transport.send(message);
        LOGGER.info("Email d'annonce envoyé à: " + email);
    }

    public void resetAccountFormreset(ActionEvent event, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/reset-password-request.fxml"));
            Parent root = loader.load();
            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setResizable(false);
            stage.show();
            LOGGER.info("Redirection réussie vers reset-password-request.fxml pour l'email: " + email);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la redirection vers reset-password-request.fxml", e);
            connAlerte.setText("Erreur lors de la redirection.");
        }
    }

    public void backlogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setResizable(false);
            stage.show();
            LOGGER.info("Redirection réussie vers login.fxml");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la redirection vers login.fxml", e);
            connAlerte.setText("Erreur lors de la redirection.");
        }
    }
}