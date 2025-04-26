package org.example;

import entite.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Objects;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    // Définir le message de bienvenue
    public void setWelcomeMessage(String message) {
        welcomeLabel.setText(message);
    }

    @FXML
    public void logout(ActionEvent event) {
        // Supprimer la session
        Session.getInstance().clearSession();

        // Rediriger vers la page de connexion
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/login.fxml")));
            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Login");
            currentStage.setResizable(true); // Allow resizing
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }
}