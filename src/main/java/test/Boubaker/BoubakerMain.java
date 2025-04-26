package test.Boubaker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class BoubakerMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Main content container
            VBox mainContent = new VBox();
            mainContent.setId("contentContainer");

            // Load header (navbar, header.fxml)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            if (headerLoader.getLocation() == null) {
                throw new Exception("header.fxml not found");
            }
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load header (banner, header.html)
            WebView headerWebView = new WebView();
            if (getClass().getResource("/header.html") != null) {
                headerWebView.getEngine().load(getClass().getResource("/header.html").toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            headerWebView.getStyleClass().add("header-webview");
            mainContent.getChildren().add(headerWebView);

            // Welcome body
            VBox bodyContent = new VBox();
            bodyContent.setId("bodyContent");
            bodyContent.getStyleClass().add("main-container");
            bodyContent.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            bodyContent.setSpacing(30);
            Label titleLabel = new Label("Welcome to Boubaker’s Module!");
            titleLabel.getStyleClass().add("main-title");
            Label messageLabel = new Label("Explore our features using the navigation above.");
            messageLabel.getStyleClass().add("pack-features");
            bodyContent.getChildren().addAll(titleLabel, messageLabel);
            VBox.setVgrow(bodyContent, Priority.ALWAYS);
            mainContent.getChildren().add(bodyContent);

            // Load footer (footer.html)
            WebView footerWebView = new WebView();
            if (getClass().getResource("/footer.html") != null) {
                footerWebView.getEngine().load(getClass().getResource("/footer.html").toExternalForm());
            } else {
                footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
            }
            footerWebView.setPrefSize(1000, 830);
            footerWebView.setId("footerWebView");
            footerWebView.getStyleClass().add("footer-webview");
            mainContent.getChildren().add(footerWebView);

            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Scene
            Scene scene = new Scene(scrollPane, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            // Fade-in transition
            FadeTransition fade = new FadeTransition(Duration.millis(500), scrollPane);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

            // Stage
            primaryStage.setTitle("Boubaker's Module");
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