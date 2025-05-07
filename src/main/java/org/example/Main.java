package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getResource("/Fonts/BubblegumSans-Regular.ttf").toExternalForm(), 14);
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/login.fxml")));
        Scene scene = new Scene(root, 828, 629);
     
     
     //   Scene scene = new Scene(root, 1500, 600);
     
     
     
        primaryStage.initStyle(StageStyle.DECORATED); // Use DECORATED for window controls
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.setResizable(true); // Allow resizing
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}