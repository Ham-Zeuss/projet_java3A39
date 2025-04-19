package test.Ham;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class CreateTitleMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting MainAppCreateTitle.start");
            String fxmlPath = "/HamzaFXML/CreateTitle.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            System.out.println("FXML URL: " + fxmlUrl);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file at " + fxmlPath);
            }

            System.out.println("Loading FXML from: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 1080, 1920);

            System.out.println("Scene created successfully");

            primaryStage.setTitle("Create Title");
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true);
            primaryStage.setWidth(1080);
            primaryStage.setHeight(1920);
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