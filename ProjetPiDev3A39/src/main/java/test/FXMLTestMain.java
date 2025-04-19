package test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import java.net.URL;
import javafx.scene.layout.AnchorPane;

public class FXMLTestMain extends Application {

    private VBox mainContent;
    private WebView headerWebView;
    private WebView footerWebView;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a VBox to stack the header, body, and footer
        mainContent = new VBox();

        // Load header.fxml
        FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
        VBox headerFxmlContent = headerFxmlLoader.load();
        headerFxmlContent.setPrefSize(1000, 100);
        mainContent.getChildren().add(headerFxmlContent);

        // Load header.html using WebView
        headerWebView = new WebView();
        URL headerUrl = getClass().getResource("/header.html");
        if (headerUrl != null) {
            headerWebView.getEngine().load(headerUrl.toExternalForm());
        } else {
            headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
        }
        headerWebView.setPrefSize(1000, 490);
        mainContent.getChildren().add(headerWebView);

        // Load initial body (AffichageModule.fxml)
        loadBody("/AffichageModule.fxml");

        // Load footer.html using WebView
        footerWebView = new WebView();
        URL footerUrl = getClass().getResource("/footer.html");
        if (footerUrl != null) {
            footerWebView.getEngine().load(footerUrl.toExternalForm());
        } else {
            footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
        }
        footerWebView.setPrefSize(1000, 830);
        mainContent.getChildren().add(footerWebView);

        // Wrap the VBox in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Set up the scene and apply CSS safely
        Scene scene = new Scene(scrollPane, 600, 400);
        URL cssUrl = getClass().getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
        if (userTitlesCssUrl != null) {
            scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
        }

        primaryStage.setTitle("JavaFX Scrollable Window");
        primaryStage.setScene(scene);

        // Pass the main application instance to the stage
        primaryStage.setUserData(this);

        primaryStage.show();
    }

    // Method to load dynamic content into the body
    public void loadBody(String fxmlPath) {
        try {
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane bodyContent = bodyLoader.load();
            bodyContent.setPrefHeight(600);
            bodyContent.setMaxHeight(600);

            // Replace the current body content
            if (mainContent.getChildren().size() > 2) {
                mainContent.getChildren().remove(2); // Remove old body
            }
            mainContent.getChildren().add(2, bodyContent); // Add new body
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}