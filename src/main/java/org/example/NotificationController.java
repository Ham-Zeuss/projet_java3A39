package org.example;

import entite.Notification;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import service.NotificationService;

public class NotificationController {
    @FXML
    private TableView<Notification> notificationTable;
    @FXML
    private TableColumn<Notification, Integer> idColumn;
    @FXML
    private TableColumn<Notification, String> messageColumn;
    @FXML
    private TableColumn<Notification, java.time.LocalDateTime> createdAtColumn;

    private final NotificationService notificationService = new NotificationService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        loadNotifications();
    }

    private void loadNotifications() {
        notificationTable.getItems().setAll(notificationService.readAll());
    }

    @FXML
    private void handleClearAll() {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Effacer toutes les notifications");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer toutes les notifications ? Cette action est irréversible.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                // Delete all notifications from the database
                notificationService.deleteAll();
                // Reload the table with animation
                loadNotificationsWithAnimation();
                showSuccess("Toutes les notifications ont été supprimées avec succès !");
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de supprimer les notifications : " + e.getMessage());
            }
        }
    }

    private void loadNotificationsWithAnimation() {
        // Initially set table opacity to 0 for fade-in effect
        notificationTable.setOpacity(0);
        notificationTable.getItems().setAll(notificationService.readAll());

        // Create and play fade-in animation
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), notificationTable);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}