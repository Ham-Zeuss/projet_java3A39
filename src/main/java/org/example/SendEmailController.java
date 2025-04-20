package org.example;

import com.gluonhq.charm.glisten.control.TextField;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Properties;
import java.util.Base64;

public class SendEmailController {

    @FXML
    private Label connAlerte;
    @FXML
    private TextArea Txtemail;
    @FXML
    private ImageView imageView;
    @FXML
    private ImageView logo2ImageView;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Button sendEmail;
    @FXML
    private TextField sendTO;

    @FXML
    void sendBtnOnAction(ActionEvent event) {
        resetAccountFormreset(event);
        String recipientEmail = sendTO.getText();
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            connAlerte.setText("Veuillez entrer une adresse email valide.");
            return;
        }
        try {
            String token = generateToken();
            saveResetToken(recipientEmail, token);
            sendEmail(recipientEmail, token);
            connAlerte.setText("Email envoyé avec succès ! Vérifiez votre boîte de réception.");
        } catch (MessagingException | IOException e) {
            connAlerte.setText("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetAccountFormreset(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/reset-password-request.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.sizeToScene();
            currentStage.setResizable(false);
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : " + e.getCause());
        }
    }

    private void sendEmail(String recipientEmail, String token) throws MessagingException, IOException {
        // SMTP configuration for Mailtrap
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");

        // Mailtrap credentials
        final String username = "628d2c7935a2da";
        final String password = "4d1baf0d355e70";

        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Load HTML template from resources
        String htmlContent;
        try (var inputStream = getClass().getResourceAsStream("/User/email_template.html")) {
            if (inputStream == null) {
                throw new IOException("Resource not found: /User/email_template.html");
            }
            htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        String resetLink = "kids://reset?token=" + token;
        htmlContent = htmlContent.replace("%RESET_LINK%", resetLink);

        // Create and send email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@example.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Reset Password KIDS");
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void saveResetToken(String email, String token) {
        // TODO: Implement logic to save token in database (ResetPasswordRequest)
        // Example: Insert into ResetPasswordRequest (email, token, expiration_date)
    }
}