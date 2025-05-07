package Controller.Boubaker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class PaiementManagementController {

    @FXML private Button viewOrdersBtn;
    @FXML private Button viewPacksBtn;
    @FXML private Button addPackBtn;

    @FXML
    public void initialize() {
        // Configure buttons with icons and text
        setupButton(viewOrdersBtn, "https://img.icons8.com/?size=100&id=112157&format=png&color=000000", "View Orders", true);
        setupButton(viewPacksBtn, "https://img.icons8.com/?size=100&id=aQlOdDXLY9k9&format=png&color=000000", "View Packs", true);
        setupButton(addPackBtn, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Add Pack", true);

        // Set button actions
        viewOrdersBtn.setOnAction(event -> openFXML("/Boubaker/view_orders.fxml", "View Orders"));
        viewPacksBtn.setOnAction(event -> openFXML("/Boubaker/view_packs.fxml", "View Packs"));
        addPackBtn.setOnAction(event -> openFXML("/Boubaker/add_pack.fxml", "Add Pack"));
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(55);
            icon.setFitHeight(55);
            button.setGraphic(icon);
            // Show text only if showText is true
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new javafx.scene.control.Tooltip(tooltipText));
            button.setMinSize(showText ? 120 : 60, 60); // Larger width for buttons with text
            // Apply specified style
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new javafx.scene.control.Tooltip(tooltipText));
            button.setMinSize(showText ? 120 : 60, 60);
            // Apply same style in fallback case
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        }
    }

    private void openFXML(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.out.println("Error: FXML not found at " + fxmlPath);
                return;
            }
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML: " + fxmlPath + " - " + e.getMessage());
        }
    }
}
