package test.Ham;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PexelGame extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Use absolute resource path to ensure correct loading
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/HamzaFXML/PexelGame.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 700);

        // Load CSS only if not already specified in FXML to avoid duplication
        if (scene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(getClass().getResource("/css/Pexel.css").toExternalForm());
        }

        stage.setTitle("Pexels Matching Game");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}