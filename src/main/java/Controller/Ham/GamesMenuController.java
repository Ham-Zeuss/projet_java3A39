package Controller.Ham;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.GaussianBlur;
import javafx.animation.TranslateTransition;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class GamesMenuController {

    @FXML private Pane scrambleButtonPane;
    @FXML private Button playScrambleGameButton;
    @FXML private Pane pexelleButtonPane;
    @FXML private Button playPexelleGameButton;
    @FXML private Pane placeholderButtonPane;
    @FXML private Button playPlaceholderGameButton;

    private static final double BUTTON_WIDTH = 150.0;
    private static final double BUTTON_HEIGHT = 50.0;
    private static final double PANE_WIDTH = 550.0;
    private static final double BLOB_RADIUS = 60.0;
    private static final double BLUR_RADIUS = 10.0;
    private static final double ANIMATION_DURATION_MS = 400.0;
    private static final double BLOB_Y_OFFSET = BUTTON_HEIGHT * 3.2;
    private static final double BLOB_HOVER_Y = -75.0;

    @FXML
    public void initialize() {
        // Initialize gooey effect for each button
        setupGooeyButton(scrambleButtonPane, playScrambleGameButton);
        setupGooeyButton(pexelleButtonPane, playPexelleGameButton);
        setupGooeyButton(placeholderButtonPane, playPlaceholderGameButton);
    }

    private void setupGooeyButton(Pane buttonPane, Button button) {
        // Create blobs
        Circle leftBlob = createBlob(-0.55);
        Circle middleBlob = createBlob(0.05);
        Circle rightBlob = createBlob(0.66);
        List<Circle> blobs = List.of(leftBlob, middleBlob, rightBlob);

        // Create transitions
        TranslateTransition leftTransition = createTransition(leftBlob, 0);
        TranslateTransition middleTransition = createTransition(middleBlob, 60);
        TranslateTransition rightTransition = createTransition(rightBlob, 25);
        List<TranslateTransition> transitions = List.of(leftTransition, middleTransition, rightTransition);

        // Clear pane and add blobs and button
        buttonPane.getChildren().clear();
        buttonPane.getChildren().addAll(blobs);
        buttonPane.getChildren().add(button);

        // Center button and blobs in the wider pane
        double centerX = (PANE_WIDTH - BUTTON_WIDTH) / 2;
        button.setLayoutX(centerX);
        button.setLayoutY(0);
        blobs.forEach(blob -> blob.setCenterX(blob.getCenterX() + centerX));
        buttonPane.setClip(new Rectangle(centerX, 0, BUTTON_WIDTH, BUTTON_HEIGHT));

        // Hover effects with event consumption
        button.setOnMouseEntered(e -> {
            transitions.forEach(t -> {
                t.setToY(BLOB_HOVER_Y);
                t.playFromStart();
            });
            button.getStyleClass().add("gooey-button-hover");
            e.consume(); // Prevent event propagation
        });

        button.setOnMouseExited(e -> {
            transitions.forEach(t -> {
                t.setToY(0);
                t.playFromStart();
            });
            button.getStyleClass().remove("gooey-button-hover");
            e.consume(); // Prevent event propagation
        });

        // Prevent pane from intercepting mouse events
        buttonPane.setPickOnBounds(false);
    }

    private Circle createBlob(double xPercent) {
        Circle blob = new Circle(BLOB_RADIUS, Color.web("#06c8d9"));
        blob.setCenterX(BUTTON_WIDTH * xPercent + BUTTON_WIDTH / 2);
        blob.setCenterY(BLOB_Y_OFFSET - BUTTON_HEIGHT); // Align behind button
        blob.setEffect(new GaussianBlur(BLUR_RADIUS));
        return blob;
    }

    private TranslateTransition createTransition(Circle blob, double delayMs) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(ANIMATION_DURATION_MS), blob);
        transition.setDelay(Duration.millis(delayMs));
        return transition;
    }

    @FXML
    private void playScrambleGame(ActionEvent event) {
        try {
            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();

            // Create a VBox to stack the header, body, and footer
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // 1. Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(screenWidth * 0.6, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // 2. Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(screenWidth);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(screenWidth * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 3. Load ScrambleGame content
            PexelController.GameController gameController = new PexelController.GameController();
            Parent gameContent = gameController.getView();
            gameContent.setStyle("-fx-pref-width: " + screenWidth + "; -fx-pref-height: " + screenHeight + "; -fx-background-color: #f0f4f8;");
            mainContent.getChildren().add(gameContent);

            // 4. Load footer
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(screenWidth);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(screenWidth * 0.6, 100, Color.LIGHTGRAY);
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
            Scene scene = new Scene(scrollPane, screenWidth, screenHeight);

            // Add CSS files
            addCommonStylesheets(scene);

            // Add ScrambleGame-specific CSS
            URL scrambleCssUrl = getClass().getResource("/css/ScrambleGameStyle.css");
            if (scrambleCssUrl != null) {
                scene.getStylesheets().add(scrambleCssUrl.toExternalForm());
            }

            // Get the stage and show the scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Word Scramble Game");
            stage.setResizable(true);

            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error launching Word Scramble Game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void playPexelleGame(ActionEvent event) {
        try {
            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();

            // Create a VBox to stack the header, body, and footer
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // 1. Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(screenWidth * 0.6, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // 2. Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(screenWidth);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(screenWidth * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 3. Load PexelGame content
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/HamzaFXML/PexelGame.fxml"));
            Parent gameContent = gameLoader.load();
            gameContent.setStyle("-fx-pref-width: " + screenWidth + "; -fx-pref-height: " + screenHeight + "; -fx-max-height: 2000;");
            mainContent.getChildren().add(gameContent);

            // 4. Load footer
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(screenWidth);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(screenWidth * 0.6, 100, Color.LIGHTGRAY);
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
            Scene scene = new Scene(scrollPane, screenWidth, screenHeight);

            // Add CSS files
            addCommonStylesheets(scene);

            // Add Pexel-specific CSS
            URL pexelCssUrl = getClass().getResource("/css/Pexel.css");
            if (pexelCssUrl != null) {
                scene.getStylesheets().add(pexelCssUrl.toExternalForm());
            }

            // Get the stage and show the scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Pexels Matching Game");
            stage.setResizable(true);

            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error launching Pexel Game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Extracted common stylesheet loading
    private void addCommonStylesheets(Scene scene) {
        URL[] cssFiles = {
                getClass().getResource("/css/store-cards.css"),
                getClass().getResource("/navbar.css"),
                getClass().getResource("/OumaimaFXML/styles.css"),
                getClass().getResource("/css/UserTitlesStyle.css"),
                getClass().getResource("/css/leaderboard.css"),
        };

        for (URL cssUrl : cssFiles) {
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS file not found: " + cssUrl);
            }
        }
    }

    @FXML
    private void Parrot(ActionEvent event) {
        try {
            // Load the PronunciationCoach FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HamzaFXML/PronunciationCoach.fxml"));
            Parent root = loader.load();

            // Create a new modal stage for the popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Makes the popup modal
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // Sets the parent window

            // Create and configure the scene
            Scene scene = new Scene(root, 1000, 750);
            popupStage.setScene(scene);
            popupStage.setTitle("Pronunciation Coach");
            popupStage.setMinWidth(900);
            popupStage.setMinHeight(700);
            popupStage.setResizable(true);
            popupStage.centerOnScreen();

            // Optionally, add CSS if PronunciationCoach has specific styles
            URL cssUrl = getClass().getResource("/css/PronunciationCoach.css"); // Adjust path if you have a CSS file
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Show the popup and wait for it to be closed
            popupStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error launching Pronunciation Coach: " + e.getMessage());
            e.printStackTrace();
        }
    }
}