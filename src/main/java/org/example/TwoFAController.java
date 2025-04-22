package org.example;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import entite.Session;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import service.TwoFactorAuthService;

import java.awt.image.BufferedImage;

public class TwoFAController {

    @FXML
    private ImageView qrCodeImage;
    @FXML
    private TextField codeInput;
    @FXML
    private Label feedbackText;

    private String secretKey;

    public void initialize() {
        TwoFactorAuthService authService = new TwoFactorAuthService();
        GoogleAuthenticatorKey key = authService.generateSecretKey();
        secretKey = key.getKey(); // 🔒 à enregistrer en DB

        try {
            String otpURL = "otpauth://totp/MonAppEducative:user@example.com?secret=" + secretKey + "&issuer=MonAppEducative";
            BitMatrix matrix = new MultiFormatWriter().encode(otpURL, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            qrCodeImage.setImage(fxImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void verifyCode(ActionEvent event) {
        try {
            int code = Integer.parseInt(codeInput.getText());
            TwoFactorAuthService authService = new TwoFactorAuthService();

            boolean isValid = authService.verifyCode(secretKey, code);
            feedbackText.setText(isValid ? "✅ Code valide" : "❌ Code invalide");

            if (isValid) {
                // Get the current session
                Session session = Session.getInstance();
                int userId = session.getUserId();
                String email = session.getEmail();

                // Load the home page
                SecurityController securityController = new SecurityController();
                securityController.loadHomePage(event, userId, email);
            }
        } catch (NumberFormatException e) {
            feedbackText.setText("❌ Veuillez entrer un code numérique valide");
        }
    }

    @FXML
    private void annnulerButtonClicked(ActionEvent event) {
        // Close the 2FA window
        Stage stage = (Stage) qrCodeImage.getScene().getWindow();
        stage.close();
    }
}