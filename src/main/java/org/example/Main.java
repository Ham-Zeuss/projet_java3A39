package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URI;
import java.util.Objects;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage; // Store the stage
        Font.loadFont(getClass().getResource("/Fonts/BubblegumSans-Regular.ttf").toExternalForm(), 14);
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/login.fxml")));
        Scene scene = new Scene(root, 828, 629);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle custom protocol for reset links
        handleCustomProtocol();
    }

    private void handleCustomProtocol() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        if (args.length > 0 && args[0].startsWith("kids://reset")) {
            processResetLink(args[0]);
        }
    }

    private void processResetLink(String uri) {
        try {
            URI resetUri = new URI(uri);
            String query = resetUri.getQuery();
            String token = query.split("token=")[1];
            showResetPasswordScreen(token, primaryStage);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du lien de réinitialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showResetPasswordScreen(String token, Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/reset_password_request.fxml"));
        Parent root = loader.load();
        ResetPasswordController controller = loader.getController();
        controller.setToken(token);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}