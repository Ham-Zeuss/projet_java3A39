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
import javafx.scene.control.Alert.AlertType;
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
            showAlert(AlertType.ERROR, "Erreur de connexion", "Veuillez remplir tous les champs !");
        } else {
            showAlert(AlertType.INFORMATION, "Connexion", "Vous êtes sur le point de vous connecter !");
        }
    }

    public void annnulerButtonClicked(ActionEvent event) {
        Stage stage = (Stage) annuler.getScene().getWindow();
        stage.close();
    }

    public void validateloginButtonClicked(ActionEvent event) throws SQLException {
        Connection connectDB = DataSource.getInstance().getConnection();
        // Inclure la colonne 'roles' dans la requête
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
                        showAlert(AlertType.ERROR, "Connexion", "Échec de connexion : Mot de passe incorrect");
                    }
                } else {
                    showAlert(AlertType.ERROR, "Connexion", "Échec de connexion : Email non trouvé");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur de connexion à la base de données : " + e.getMessage());
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
                showAlert(AlertType.ERROR, "Erreur", "Impossible de charger la page 2FA : stage is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger la page 2FA : " + e.getMessage());
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

    public void loadHomePage(ActionEvent event, int userId, String email) {
        try {
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

            // Load body (User/Home.fxml)
            String fxmlPath = "/User/Home.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new Exception("Home.fxml not found at path: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            VBox bodyContent = loader.load();
            bodyContent.setPrefSize(1920, 1080);

            // Set welcome message in HomeController
            HomeController homeController = loader.getController();
            if (homeController != null) {
                homeController.setWelcomeMessage("Bienvenue ID: " + userId);
            } else {
                System.err.println("HomeController is null");
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
            scene.getStylesheets().add(getClass().getResource("/navbar.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/UserTitlesStyle.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/store-cards.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/leaderboard.css").toExternalForm());

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            currentStage.setScene(scene);
                            currentStage.setTitle("Home");
                            currentStage.setWidth(1920); // Initial width
                            currentStage.setHeight(1080); // Initial height
                            currentStage.setResizable(true); // Allow resizing
                            currentStage.setFullScreen(false); // Ensure not full-screen
                            currentStage.centerOnScreen();
                            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    public void createAccountFormin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/register.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Register");
            currentStage.setResizable(true); // Allow resizing
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
            currentStage.setResizable(true); // Allow resizing
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : " + e.getCause());
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}