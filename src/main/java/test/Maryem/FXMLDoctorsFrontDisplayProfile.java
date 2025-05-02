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

public class FXMLDoctorsFrontDisplayProfile extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a VBox to stack the header image, body, and footer
        VBox mainContent = new VBox();
        mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

        // 1. Add header image
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

            // Add some spacing below the image if needed
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

        // 2. Load body (FrontDoctorsDisplayProfiles.fxml)
        URL fxmlResource = getClass().getResource("/MaryemFXML/FrontDoctorsDisplayProfiles.fxml");
        if (fxmlResource == null) {
            throw new IOException("Could not find FrontDoctorsDisplayProfiles.fxml at /MaryemFXML/FrontDoctorsDisplayProfiles.fxml");
        }
        System.out.println("Loading FrontDoctorsDisplayProfiles.fxml from: " + fxmlResource.toExternalForm());

        FXMLLoader bodyLoader = new FXMLLoader(fxmlResource);
        VBox bodyContent;
        try {
            bodyContent = bodyLoader.load();
        } catch (IOException e) {
            System.err.println("Failed to load FrontDoctorsDisplayProfiles.fxml: " + e.getMessage());
            throw e;
        }
        bodyContent.setStyle("-fx-pref-width: 1920; -fx-pref-height: 1080; -fx-max-height: 2000;");
        bodyContent.setStyle(bodyContent.getStyle() + "; -fx-background-color: #B8DAB8FF;"); // here

        mainContent.getChildren().add(bodyContent);

        // 3. Load footer as ImageView
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
        double totalHeight = headerImageView.getFitHeight() +
                bodyContent.prefHeight(-1) +
                footerImageView.getFitHeight();

        // Set scene to specified window size
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