package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mes_rendezvous extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the UserConsultations.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserConsultations.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.setTitle("User Appointments");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}