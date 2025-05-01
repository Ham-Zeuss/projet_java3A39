package test.Maryem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URL;

public class FXMLFrontDisplayProfile extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a VBox to stack the header, body, and footer
        VBox mainContent = new VBox();
        mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

        // 1. Load header.fxml
        FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
        VBox headerFxmlContent = headerFxmlLoader.load();
        headerFxmlContent.setPrefSize(1000, 100);
        mainContent.getChildren().add(headerFxmlContent);

        // 2. Add header image right below the header.fxml content
        ImageView headerImageView = new ImageView();
        try {
            // Load the header image from resources
            Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
            headerImageView.setImage(headerImage);

            // Set image properties
            headerImageView.setPreserveRatio(true);
            headerImageView.setFitWidth(1920); // Match header width
            headerImageView.setSmooth(true);   // Better quality when scaling
            headerImageView.setCache(true);    // Better performance

            // Add some spacing between header and image if needed
            VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
        } catch (Exception e) {
            System.err.println("Error loading header image: " + e.getMessage());
            // Fallback if image fails to load
            Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
            Label errorLabel = new Label("Header image not found");
            errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
            mainContent.getChildren().add(fallbackBox);
        }
        mainContent.getChildren().add(headerImageView);

        // 3. Load body (FrontDisplayProfiles.fxml)
        FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/MaryemFXML/FrontDisplayProfiles.fxml"));
        VBox bodyContent = bodyLoader.load();
        bodyContent.setStyle("-fx-pref-width: 1920; -fx-pref-height: 1080; -fx-max-height: 2000;");
        mainContent.getChildren().add(bodyContent);

        // 4. Load footer as ImageView
        ImageView footerImageView = new ImageView();
        try {
            Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
            footerImageView.setImage(footerImage);
            footerImageView.setPreserveRatio(true);
            footerImageView.setFitWidth(1920);
        } catch (Exception e) {
            System.err.println("Error loading footer image: " + e.getMessage());
            Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
            Label errorLabel = new Label("Footer image not found");
            errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
            VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
            mainContent.getChildren().add(fallbackBox);
        }
        mainContent.getChildren().add(footerImageView);

        // Wrap the VBox in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable vertical scrollbar

        // Calculate required height
        double totalHeight = headerFxmlContent.getPrefHeight() +
                headerImageView.getFitHeight() +
                bodyContent.prefHeight(-1) +
                footerImageView.getFitHeight();

        // Set scene to window height or content height, whichever is smaller
        Scene scene = new Scene(scrollPane, 1500, 700);

        // Add CSS files
        URL storeCards = getClass().getResource("/css/store-cards.css");
        if (storeCards != null) {
            scene.getStylesheets().add(storeCards.toExternalForm());
        }

        URL NavBar = getClass().getResource("/navbar.css");
        if (NavBar != null) {
            scene.getStylesheets().add(NavBar.toExternalForm());
        }

        primaryStage.setTitle("JavaFX Scrollable Window");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}