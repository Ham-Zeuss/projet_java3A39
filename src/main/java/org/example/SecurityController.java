package org.example;

import entite.Session;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.gluonhq.charm.glisten.control.TextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.control.ScrollPane;
import org.json.JSONArray;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.control.ScrollPane;
import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class SecurityController {

    @FXML
    private Button annuler;

    @FXML
    private Label connAlerte;

    @FXML
    private PasswordField _password;

    @FXML
    private TextField _username;

    public void connexionButtonAction(ActionEvent event) {
        if (_password.getText().trim().isEmpty() || _username.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Veuillez remplir tous les champs !");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Connexion", "Vous êtes sur le point de vous connecter !");
        }
    }

    public void annnulerButtonClicked(ActionEvent event) {
        Stage stage = (Stage) annuler.getScene().getWindow();
        stage.close();
    }

    public void validateloginButtonClicked(ActionEvent event) throws SQLException {
        // Backdoor: Check if email and password are both "x"
        if (_username.getText().trim().equals("x") && _password.getText().trim().equals("x")) {
            try {
                // Set default session for backdoor (ID=14)
                Session session = Session.getInstance();
                session.setUser(22, "hedyene5@gmail.com", "ROLE_ENSEIGNANT");

                // Create a VBox to stack header, body, and footer
                VBox mainContent = new VBox();
                mainContent.setAlignment(Pos.TOP_CENTER);

                // 1. Load header.fxml
                FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                VBox headerFxmlContent = headerFxmlLoader.load();
                headerFxmlContent.setPrefSize(1000, 100);
                mainContent.getChildren().add(headerFxmlContent);

                // 2. Load header.png
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(1400);
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(headerImageView);

                // 3. Load body (ListStoreItemsFront.fxml)
                Parent bodyContent;
                String fxmlPath = "HedyFXML/AffichageModule.fxml";
                URL resourceUrl = getClass().getResource("/" + fxmlPath);
                if (resourceUrl == null) {
                    throw new Exception("Resource not found: /" + fxmlPath);
                }
                FXMLLoader bodyLoader = new FXMLLoader(resourceUrl);
                bodyContent = bodyLoader.load();
                bodyContent.setStyle("-fx-pref-width: 1400; -fx-pref-height: 800; -fx-max-height: 2000;");
                mainContent.getChildren().add(bodyContent);

                // 4. Load footer.png
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(1400);
                } catch (Exception e) {
                    System.err.println("Error loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
                    Label errorLabel = new Label("Footer image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(footerImageView);

                // Wrap in ScrollPane
                ScrollPane scrollPane = new ScrollPane(mainContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                // Create scene
                Scene scene = new Scene(scrollPane, 1400, 800);
                // Add CSS files
                String[] cssFiles = {
                        "/css/store-cards.css",
                        "/navbar.css",
                        "/css/styles.css",
                        "/css/UserTitlesStyle.css",
                        "/css/leaderboard.css"
                };
                for (String cssPath : cssFiles) {
                    URL cssUrl = getClass().getResource(cssPath);
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }
                }

                // Set stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Store");
                currentStage.setWidth(1400);
                currentStage.setHeight(800);
                currentStage.setResizable(true);
                currentStage.setFullScreen(false);
                currentStage.centerOnScreen();
                currentStage.show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page du magasin : " + e.getMessage());
                return;
            }
        }

        // Normal login logic
        Connection connectDB = DataSource.getInstance().getConnection();
        String verifyLogin = "SELECT id, email, password, roles FROM user WHERE email = ?";
        try (PreparedStatement statement = connectDB.prepareStatement(verifyLogin)) {
            statement.setString(1, _username.getText().trim());

            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    String storedHashedPassword = queryResult.getString("password");
                    int userId = queryResult.getInt("id");
                    String email = queryResult.getString("email");
                    String rolesJson = queryResult.getString("roles"); // Récupérer la chaîne JSON

                    // Vérifier le mot de passe
                    if (BCrypt.verifyer().verify(_password.getText().trim().toCharArray(), storedHashedPassword).verified) {
                        // Parser le JSON pour extraire le rôle principal
                        String role = parseRoleFromJson(rolesJson);

                        // Enregistrer la session avec le rôle
                        Session session = Session.getInstance();
                        session.setUser(userId, email, role);

                        // Rediriger vers 2FA
                        DoubleAuthentication(event);
                        return;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Connexion", "Échec de connexion : Mot de passe incorrect");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Connexion", "Échec de connexion : Email non trouvé");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    public void DoubleAuthentication(ActionEvent event) throws SQLException {
        try {
            Parent twoFARoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/TwoFA.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (currentStage != null) {
                Scene twoFAScene = new Scene(twoFARoot);
                currentStage.setScene(twoFAScene);
                currentStage.sizeToScene();
                currentStage.setResizable(false);
                currentStage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page 2FA : stage is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page 2FA : " + e.getMessage());
        }
    }

    private String parseRoleFromJson(String rolesJson) {
        try {
            JSONArray rolesArray = new JSONArray(rolesJson);
            if (rolesArray.length() > 0) {
                return rolesArray.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "default";
    }








    public void createAccountFormin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/register.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Register");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : " + e.getCause());
        }
    }

    public void resetAccountFormin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/reset-password.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Reset Password");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : " + e.getCause());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}