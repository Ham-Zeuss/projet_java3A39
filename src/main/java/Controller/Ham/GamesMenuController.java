package Controller.Ham;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import test.Ham.GameController;

import java.io.IOException;
import java.net.URL;

public class GamesMenuController {

    @FXML
    private void playScrambleGame(ActionEvent event) {
        try {
            // Create a VBox to stack header, game, and footer
            VBox mainContent = new VBox();

            // Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load header.html
            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            // Load ScrambleGame content
            GameController gameController = new GameController();
            Parent gameContent = gameController.getView();
            gameContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 500; -fx-background-color: #f0f4f8;");
            mainContent.getChildren().add(gameContent);

            // Load footer.html
            WebView footerWebView = new WebView();
            URL footerUrl = getClass().getResource("/footer.html");
            if (footerUrl != null) {
                footerWebView.getEngine().load(footerUrl.toExternalForm());
            } else {
                footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
            }
            footerWebView.setPrefSize(1000, 830);
            mainContent.getChildren().add(footerWebView);

            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Set up the scene with stylesheets
            Scene scene = new Scene(scrollPane, 600, 400);
            URL cssUrl = getClass().getResource("/OumaimaFXML/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
            if (userTitlesCssUrl != null) {
                scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
            }
            URL storeCardsCssUrl = getClass().getResource("/css/store-cards.css");
            if (storeCardsCssUrl != null) {
                scene.getStylesheets().add(storeCardsCssUrl.toExternalForm());
            }
            URL leaderboardCssUrl = getClass().getResource("/css/leaderboard.css");
            if (leaderboardCssUrl != null) {
                scene.getStylesheets().add(leaderboardCssUrl.toExternalForm());
            }
            URL gameCssUrl = getClass().getResource("/css/ScrambleGameStyle.css");
            if (gameCssUrl != null) {
                scene.getStylesheets().add(gameCssUrl.toExternalForm());
            }

            // Get the stage and show the scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Word Scramble Game");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error launching Word Scramble Game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}