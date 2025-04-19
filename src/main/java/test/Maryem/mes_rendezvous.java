package test.Maryem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import java.net.URL;
import java.io.IOException;

public class mes_rendezvous extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create a VBox to stack the header, header.fxml, body, and footer
            VBox mainContent = new VBox();

            // Load header.fxml (additional section below header.html)
            System.out.println("Loading header.fxml");
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            if (headerFxmlLoader.getLocation() == null) {
                throw new IOException("Cannot find header.fxml at /header.fxml");
            }
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);
            System.out.println("header.fxml loaded successfully");

            // Load header (header.html) using WebView
            System.out.println("Loading header.html");
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
            System.out.println("header.html loaded successfully");

            // Load body (UserConsultations.fxml)
            System.out.println("Loading UserConsultations.fxml");
            URL bodyUrl = getClass().getResource("/MaryemFXML/UserConsultations.fxml");
            if (bodyUrl == null) {
                throw new IOException("Cannot find UserConsultations.fxml at /UserConsultations.fxml");
            }
            FXMLLoader bodyLoader = new FXMLLoader(bodyUrl);
            VBox bodyContent = bodyLoader.load();
            bodyContent.setPrefHeight(600);
            bodyContent.setMaxHeight(600);
            mainContent.getChildren().add(bodyContent);
            System.out.println("UserConsultations.fxml loaded successfully");

            // Load footer (footer.html) using WebView
            System.out.println("Loading footer.html");
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
            System.out.println("footer.html loaded successfully");

            // Wrap the VBox in a ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Set up the scene and apply CSS
            Scene scene = new Scene(scrollPane, 600, 400);
            URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("styles.css applied successfully");
            } else {
                System.err.println("Error: styles.css not found in resources at /css/styles.css");
            }
            URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
            if (userTitlesCssUrl != null) {
                scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
                System.out.println("UserTitlesStyle.css applied successfully");
            } else {
                System.err.println("Error: UserTitlesStyle.css not found in resources at /css/UserTitlesStyle.css");
            }
            URL consultationsCssUrl = getClass().getResource("/css/ConsultationsStyle.css");
            if (consultationsCssUrl != null) {
                scene.getStylesheets().add(consultationsCssUrl.toExternalForm());
                System.out.println("ConsultationsStyle.css applied successfully");
            } else {
                System.err.println("Error: ConsultationsStyle.css not found in resources at /css/ConsultationsStyle.css");
            }

            primaryStage.setTitle("JavaFX Scrollable Window - User Appointments");
            primaryStage.setScene(scene);
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