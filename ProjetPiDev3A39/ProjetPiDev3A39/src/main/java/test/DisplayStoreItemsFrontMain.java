package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class DisplayStoreItemsFrontMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting DisplayStoreItemsFrontMain.start");
            String fxmlPath = "/fxml/ListStoreItemsFront.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            System.out.println("FXML URL: " + fxmlUrl);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file at " + fxmlPath);
            }

            System.out.println("Loading FXML from: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 800, 400);
            System.out.println("Scene created successfully");

            primaryStage.setTitle("Store Items - User");
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("Stage shown successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load user interface: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting JavaFX user application");
        launch(args);
    }
}