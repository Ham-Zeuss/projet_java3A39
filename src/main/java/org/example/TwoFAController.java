package org.example;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import entite.Session;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.TwoFactorAuthService;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
        secretKey = key.getKey();

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
                Session session = Session.getInstance();
                String role = session.getRole();

                Map<String, String> roleToFxmlMap = new HashMap<>();
                roleToFxmlMap.put("ROLE_MEDECIN", "/MaryemFXML/FrontDoctorsDisplayProfiles.fxml");
                roleToFxmlMap.put("ROLE_ENSEIGNANT", "/HedyFXML/AffichageCours.fxml");
                roleToFxmlMap.put("ROLE_PARENT", "/User/Home.fxml");

                String defaultFxml = "/User/Home.fxml";
                String fxmlPath = roleToFxmlMap.getOrDefault(role, defaultFxml);

                VBox mainContent = new VBox();




                mainContent.setAlignment(Pos.TOP_CENTER);

                // Load header image (PNG)
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(1920);
                    headerImageView.setSmooth(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                    mainContent.getChildren().add(headerImageView);
                } catch (Exception e) {
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    mainContent.getChildren().add(errorLabel);
                }

                // Load role-specific body
                URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    throw new Exception("FXML file not found at path: " + fxmlPath);
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                VBox bodyContent = loader.load();
                bodyContent.setPrefSize(1920, 1080);

                if ("ROLE_MEDECIN".equals(role)) {
                    bodyContent.setStyle("-fx-background-color: #B8DAB8FF;");
                }

                if ("ROLE_PARENT".equals(role)) {

                    // Load header.fxml
                    FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                    VBox headerFxmlContent = headerFxmlLoader.load();
                    headerFxmlContent.setPrefSize(1000, 100);
                    mainContent.getChildren().add(headerFxmlContent);

                    HomeController homeController = loader.getController();
                    if (homeController != null) {
                        homeController.setWelcomeMessage("Bienvenue ID: " + session.getUserId());
                    }
                }

                mainContent.getChildren().add(bodyContent);

                // Load footer image (PNG)
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(1920);
                    footerImageView.setSmooth(true);
                    mainContent.getChildren().add(footerImageView);
                } catch (Exception e) {
                    Label errorLabel = new Label("Footer image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    mainContent.getChildren().add(errorLabel);
                }

                // Wrap VBox in ScrollPane
                ScrollPane scrollPane = new ScrollPane(mainContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                // Set scene
                Scene scene = new Scene(scrollPane, 1920, 1080);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/UserTitlesStyle.css").toExternalForm());

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Dashboard - " + role);
                currentStage.setFullScreen(true);
                currentStage.setWidth(1920);
                currentStage.setHeight(1080);
                currentStage.centerOnScreen();
                currentStage.show();
            }
        } catch (NumberFormatException e) {
            feedbackText.setText("❌ Veuillez entrer un code numérique valide");
        } catch (Exception e) {
            e.printStackTrace();
            feedbackText.setText("❌ Erreur lors du chargement de la page");
        }
    }


    @FXML
    private void annnulerButtonClicked(ActionEvent event) {
        Stage stage = (Stage) qrCodeImage.getScene().getWindow();
        stage.close();
    }
}
