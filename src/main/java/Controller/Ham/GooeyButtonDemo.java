package Controller.Ham;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.effect.GaussianBlur;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.geometry.Insets;
import java.util.List;

public class GooeyButtonDemo extends Application {

    private static final double BUTTON_WIDTH = 150.0;
    private static final double BUTTON_HEIGHT = 50.0;
    private static final double BLOB_RADIUS = 60.0;
    private static final double BLUR_RADIUS = 10.0;
    private static final double ANIMATION_DURATION_MS = 400.0;
    private static final double BLOB_Y_OFFSET = BUTTON_HEIGHT * 2.2;
    private static final double BLOB_HOVER_Y = -75.0;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0;");

        // Create button
        Button button = new Button("HOVER ME");
        button.setLayoutX(50);
        button.setLayoutY(50);
        button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.getStyleClass().addAll("gooey-button", "gooey-button-normal");

        // Create blobs
        Circle leftBlob = createBlob(-0.55, BLOB_Y_OFFSET);
        Circle middleBlob = createBlob(0.05, BLOB_Y_OFFSET);
        Circle rightBlob = createBlob(0.66, BLOB_Y_OFFSET);
        List<Circle> blobs = List.of(leftBlob, middleBlob, rightBlob);

        // Create transitions
        TranslateTransition leftTransition = createTransition(leftBlob, 0);
        TranslateTransition middleTransition = createTransition(middleBlob, 60);
        TranslateTransition rightTransition = createTransition(rightBlob, 25);
        List<TranslateTransition> transitions = List.of(leftTransition, middleTransition, rightTransition);

        // Set up blobs pane
        Pane blobsPane = new Pane(blobs.toArray(new Circle[0]));
        blobsPane.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        blobsPane.setLayoutX(50);
        blobsPane.setLayoutY(50);
        blobsPane.setClip(new javafx.scene.shape.Rectangle(BUTTON_WIDTH, BUTTON_HEIGHT));

        // Hover effects
        button.setOnMouseEntered(e -> {
            transitions.forEach(t -> {
                t.setToY(BLOB_HOVER_Y);
                t.playFromStart();
            });
            button.getStyleClass().remove("gooey-button-normal");
            button.getStyleClass().add("gooey-button-hover");
        });

        button.setOnMouseExited(e -> {
            transitions.forEach(t -> {
                t.setToY(0);
                t.playFromStart();
            });
            button.getStyleClass().remove("gooey-button-hover");
            button.getStyleClass().add("gooey-button-normal");
        });

        // Assemble scene
        root.getChildren().addAll(blobsPane, button);
        Scene scene = new Scene(root, 300, 200);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/GooButton.css").toExternalForm());
        } catch (Exception ex) {
            System.err.println("Failed to load stylesheet: " + ex.getMessage());
        }

        primaryStage.setTitle("Gooey Button in JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Circle createBlob(double xPercent, double yOffset) {
        Circle blob = new Circle(BLOB_RADIUS, Color.web("#06c8d9"));
        blob.setCenterX(BUTTON_WIDTH * xPercent + BUTTON_WIDTH / 2);
        blob.setCenterY(yOffset);
        blob.setEffect(new GaussianBlur(BLUR_RADIUS));
        return blob;
    }

    private TranslateTransition createTransition(Circle blob, double delayMs) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(ANIMATION_DURATION_MS), blob);
        transition.setDelay(Duration.millis(delayMs));
        return transition;
    }

    public static void main(String[] args) {
        launch(args);
    }
}