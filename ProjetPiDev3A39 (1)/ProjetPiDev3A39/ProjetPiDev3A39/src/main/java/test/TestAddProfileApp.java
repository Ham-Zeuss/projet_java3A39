package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestAddProfileApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML from resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddProfile.fxml"));
        Scene scene = new Scene(loader.load(), 600, 450);
        stage.setTitle("Test Add Profile");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}