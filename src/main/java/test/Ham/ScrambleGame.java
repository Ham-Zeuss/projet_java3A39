package test.Ham;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScrambleGame extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameController controller = new GameController();
        Scene scene = new Scene(controller.getView(), 400, 500);
        primaryStage.setTitle("Word Scramble Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}