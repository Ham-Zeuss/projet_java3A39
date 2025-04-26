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
import javafx.scene.Node;
import javafx.scene.Parent;
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
                // Récupérer la session
                Session session = Session.getInstance();
                String role = session.getRole();

                // Mappage des rôles aux chemins FXML
                Map<String, String> roleToFxmlMap = new HashMap<>();
                roleToFxmlMap.put("ROLE_MEDECIN", "/MaryemFXML/FrontDoctorsDisplayProfiles.fxml");
                roleToFxmlMap.put("ROLE_ENSEIGNANT", "/HedyFXML/AffichageCours.fxml");
                roleToFxmlMap.put("ROLE_PARENT", "/User/Home.fxml");

                String defaultFxml = "/User/Home.fxml"; // Page par défaut
                String fxmlPath = roleToFxmlMap.getOrDefault(role, defaultFxml);

                // Create a VBox to stack the header, header.fxml, body, and footer
                VBox mainContent = new VBox();

                // Load header.fxml
                FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                VBox headerFxmlContent = headerFxmlLoader.load();
                headerFxmlContent.setPrefSize(1000, 100);
                mainContent.getChildren().add(headerFxmlContent);

                // Load header (header.html) using WebView
                WebView headerWebView = new WebView();
                URL headerUrl = getClass().getResource("/header.html");
                if (headerUrl != null) {
                    headerWebView.getEngine().load(headerUrl.toExternalForm());
                } else {
                    headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
                }
                headerWebView.setPrefSize(1000, 490);
                mainContent.getChildren().add(headerWebView);

                // Load body (based on role)
                URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    throw new Exception("FXML file not found at path: " + fxmlPath);
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                VBox bodyContent = loader.load();
                bodyContent.setPrefSize(1920, 1080);

                // Set welcome message if loading Home.fxml
                if (fxmlPath.equals("/User/Home.fxml")) {
                    HomeController homeController = loader.getController();
                    if (homeController != null) {
                        homeController.setWelcomeMessage("Bienvenue ID: " + session.getUserId());
                    } else {
                        System.err.println("HomeController is null");
                    }
                }
                mainContent.getChildren().add(bodyContent);

                // Load footer (footer.html) using WebView
                WebView footerWebView = new WebView();
                URL footerUrl = getClass().getResource("/footer.html");
                if (footerUrl != null) {
                    footerWebView.getEngine().load(footerUrl.toExternalForm());
                } else {
                    footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
                }
                footerWebView.setPrefSize(1000, 830);
                mainContent.getChildren().add(footerWebView);

                // Wrap the VBox in a ScrollPane
                ScrollPane scrollPane = new ScrollPane(mainContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                // Set up the scene and apply CSS
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
        // Close the 2FA window
        Stage stage = (Stage) qrCodeImage.getScene().getWindow();
        stage.close();
    }
}