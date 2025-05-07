package test.Oumaima;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.Parent;
import java.net.URL;

public class FXMLAffichageQuizFront extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox mainContent = new VBox();
        mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

        // 1. Add header image as the first element
        ImageView headerImageView = new ImageView();
        try {
            // Load the header image from resources
            Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
            headerImageView.setImage(headerImage);

            // Set image properties
            headerImageView.setPreserveRatio(true); // Correct method for JavaFX ImageView
            headerImageView.setFitWidth(1500); // Match header width
            headerImageView.setSmooth(true);   // Better quality when scaling
            headerImageView.setCache(true);    // Better performance

            // Add some spacing below the header image
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

        // 2. Load body (affichageQuiz.fxml)
        FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
        if (bodyLoader.getLocation() == null) {
            throw new IllegalStateException("Fichier /OumaimaFXML/affichageQuiz.fxml introuvable");
        }
        Parent bodyContent = bodyLoader.load();
        bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
        mainContent.getChildren().add(bodyContent);

        // 3. Load footer as ImageView
        ImageView footerImageView = new ImageView();
        try {
            Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
            footerImageView.setImage(footerImage);
            footerImageView.setPreserveRatio(true); // Correct method for JavaFX ImageView
            footerImageView.setFitWidth(1500);
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

        // Set scene to specified size
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

        primaryStage.setTitle("JavaFX Quiz Display");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}