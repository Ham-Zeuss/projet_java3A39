package test.Ham;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class DisplayTitlesMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting MainAppTitles.start");
            String fxmlPath = "/HamzaFXML/ListTitles.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            System.out.println("FXML URL: " + fxmlUrl);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file at " + fxmlPath);
            }

            System.out.println("Loading FXML from: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 1920, 1080);

            System.out.println("Scene created successfully");

            primaryStage.setTitle("List Titles");
            primaryStage.setScene(scene);
            // Set full-screen mode
            primaryStage.setFullScreen(true);
            primaryStage.setWidth(1920);
            primaryStage.setHeight(1080);
            primaryStage.centerOnScreen();
            primaryStage.show();
            System.out.println("Stage shown successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load interface: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting JavaFX application");
        launch(args);
    }
}