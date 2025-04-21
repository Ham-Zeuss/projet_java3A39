package org.example;
import entite.Session;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.gluonhq.charm.glisten.control.TextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
        String verifyLogin = "SELECT id, email, password FROM user WHERE email = ?"; // Inclure email et id
        try (PreparedStatement statement = connectDB.prepareStatement(verifyLogin)) {
            statement.setString(1, _username.getText().trim());

            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    String storedHashedPassword = queryResult.getString("password");
                    int userId = queryResult.getInt("id");
                    String email = queryResult.getString("email");

                    if (BCrypt.verifyer().verify(_password.getText().trim().toCharArray(), storedHashedPassword).verified) {
                        // Enregistrer la session
                        Session session = Session.getInstance();
                        session.setUser(userId, email);

                        // Charger home.fxml
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/home.fxml"));
                            Parent root = loader.load();

                            // Accéder au contrôleur de la page home
                            HomeController homeController = loader.getController();
                            if (homeController != null) {
                                homeController.setWelcomeMessage("Bienvenue ID: " + userId);
                            }

                            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            Scene scene = new Scene(root);
                            currentStage.setScene(scene);
                            currentStage.sizeToScene();
                            currentStage.setResizable(false);
                            currentStage.show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil : " + e.getMessage());
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Connexion", "Échec de connexion");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Connexion", "Échec de connexion");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    public void createAccountFormin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/register.fxml")));
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

    public void resetAccountFormin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/reset-password.fxml")));
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

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
