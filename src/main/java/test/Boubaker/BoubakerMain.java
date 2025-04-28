package test.Boubaker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import Controller.Boubaker.MainBoubakerController;
import java.net.URL;

public class BoubakerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Main container - using StackPane as you requested
        StackPane windowRoot = new StackPane();
        windowRoot.getStyleClass().add("window-root");

        // Scrollable content container
        ScrollPane mainScrollPane = new ScrollPane();
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.getStyleClass().add("main-scroll");

        // Content container (will hold header, body, footer)
        VBox contentContainer = new VBox();
        contentContainer.getStyleClass().add("content-container");

        try {
            // 1. HEADER (always visible)
            VBox headerContainer = new VBox();
            headerContainer.getStyleClass().add("header-container");

            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));

            VBox headerFxmlContent = headerFxmlLoader.load();


            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/hheader.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            }
            headerContainer.getChildren().addAll(headerFxmlContent, headerWebView);

            // 2. BODY CONTENT (expandable)
            MainBoubakerController mainBoubakerController = new MainBoubakerController();
            VBox bodyContent = mainBoubakerController.getRoot();
            bodyContent.getStyleClass().add("body-content");

            // Make body content expandable
            VBox.setVgrow(bodyContent, Priority.ALWAYS);

            // 3. FOOTER (always visible, larger size)
            WebView footerWebView = new WebView();
            URL footerUrl = getClass().getResource("/footer.html");
            if (footerUrl != null) {
                footerWebView.getEngine().load(footerUrl.toExternalForm());
            }
            footerWebView.getStyleClass().add("footer-webview");

            // Create a container for the footer to control its size
            StackPane footerContainer = new StackPane(footerWebView);
            footerContainer.getStyleClass().add("footer-container");

            // Assemble everything
            contentContainer.getChildren().addAll(headerContainer, bodyContent, footerContainer);
            mainScrollPane.setContent(contentContainer);
            windowRoot.getChildren().add(mainScrollPane);

            // Scene setup
            Scene scene = new Scene(windowRoot, 1000, 700); // Increased window size
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            primaryStage.setTitle("KPI Packs");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(650);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Application startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}