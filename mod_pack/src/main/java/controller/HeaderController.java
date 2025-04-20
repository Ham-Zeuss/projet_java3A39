package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HeaderController {
    private Stage mainStage;

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @FXML
    private void showMain() {
        // Already in main window, do nothing
    }

    @FXML
    private void showOrders() {
        openNewWindow("/orders.fxml", "Orders");
    }

    @FXML
    private void showChatbot() {
        openNewWindow("/chatbot.fxml", "Chatbot");
    }

    @FXML
    private void showBackOffice() {
        openNewWindow("/backoffice.fxml", "Back Office");
    }

    private void openNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            VBox content = loader.load();
            VBox root = new VBox();
            Button closeButton = new Button("Close");
            closeButton.getStyleClass().add("action-button");
            closeButton.setOnAction(e -> {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
            });
            HBox closeBox = new HBox(closeButton);
            closeBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            closeBox.setPadding(new javafx.geometry.Insets(5));
            root.getChildren().addAll(closeBox, content);
            Stage newStage = new Stage();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            newStage.setTitle(title);
            newStage.setScene(scene);
            newStage.setResizable(true);
            newStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}