package test.Oumaima;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.Parent;
import java.net.URL;

public class FXMLAffichageQuizFront extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox mainContent = new VBox();

        // Load header.fxml
        FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
        VBox headerFxmlContent = headerFxmlLoader.load();
        headerFxmlContent.setPrefSize(800, 100); // Adjusted to match AnchorPane width
        mainContent.getChildren().add(headerFxmlContent);

        // Load header.html
        WebView headerWebView = new WebView();
        URL headerUrl = getClass().getResource("/header.html");
        if (headerUrl != null) {
            headerWebView.getEngine().load(headerUrl.toExternalForm());
        } else {
            headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
        }
        headerWebView.setPrefSize(800, 100); // Reduced height
        mainContent.getChildren().add(headerWebView);

        // Load body (affichageQuiz.fxml)
        FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
        Parent bodyContent = bodyLoader.load();
        bodyContent.setStyle("-fx-pref-width: 800; -fx-pref-height: 400;"); // Adjusted height to fit
        mainContent.getChildren().add(bodyContent);

        // Load footer.html
        WebView footerWebView = new WebView();
        URL footerUrl = getClass().getResource("/footer.html");
        if (footerUrl != null) {
            footerWebView.getEngine().load(footerUrl.toExternalForm());
        } else {
            footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
        }
        footerWebView.setPrefSize(800, 100); // Reduced height
        mainContent.getChildren().add(footerWebView);

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Set up the scene
        Scene scene = new Scene(scrollPane, 800, 600);
        URL cssUrl = getClass().getResource("/OumaimaFXML/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
        if (userTitlesCssUrl != null) {
            scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
        }

        primaryStage.setTitle("JavaFX Quiz Display");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}