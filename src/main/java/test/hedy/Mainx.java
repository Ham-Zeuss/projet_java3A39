package test.hedy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import java.net.URL;

public class Mainx extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a VBox to stack the header, header.fxml, body, and footer
        VBox mainContent = new VBox();

        // Load header.fxml (additional section below header.html)
        FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
        VBox headerFxmlContent = headerFxmlLoader.load(); // Ensure header.fxml has VBox root
        headerFxmlContent.setPrefSize(1000, 100);
        mainContent.getChildren().add(headerFxmlContent);

        // Load header (header.html) using WebView
        WebView headerWebView = new WebView();
        URL headerUrl = getClass().getResource("/header.html");
        if (headerUrl != null) {
            System.out.println("Header URL: " + headerUrl.toExternalForm());
            headerWebView.getEngine().load(headerUrl.toExternalForm());
        } else {
            System.err.println("Error: header.html not found");
            headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
        }
        headerWebView.setPrefSize(1000, 490);
        mainContent.getChildren().add(headerWebView);

        // Load body (AffichageCours.fxml)
        FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageModule.fxml"));
        AnchorPane bodyContent = bodyLoader.load(); // Changed to AnchorPane
        bodyContent.setPrefHeight(600);
        bodyContent.setMaxHeight(600);
        mainContent.getChildren().add(bodyContent);

        // Load footer (footer.html) using WebView
        WebView footerWebView = new WebView();
        URL footerUrl = getClass().getResource("/footer.html");
        if (footerUrl != null) {
            System.out.println("Footer URL: " + footerUrl.toExternalForm());
            footerWebView.getEngine().load(footerUrl.toExternalForm());
        } else {
            System.err.println("Error: footer.html not found");
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
        } else {
            System.err.println("Error: styles.css not found in resources at /css/styles.css");
        }
        // Add UserTitlesStyle.css for title cards
        URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
        if (userTitlesCssUrl != null) {
            scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
        } else {
            System.err.println("Error: UserTitlesStyle.css not found in resources at /css/UserTitlesStyle.css");
        }

        primaryStage.setTitle("JavaFX Scrollable Window");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}