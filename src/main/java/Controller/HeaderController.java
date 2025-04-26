package Controller;

import Controller.Maryem.FrontDisplayProfilesController;
import entite.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

public class HeaderController {

    @FXML
    private MenuButton learnMenuButton; // Reference to Learn MenuButton

    @FXML
    private MenuButton doctorsMenuButton; // Reference to Doctors MenuButton

    @FXML
    private MenuButton gamesMenuButton; // Reference to Games MenuButton

    /**
     * Centralized method to handle navigation to different pages.
     * @param event The ActionEvent triggered by a MenuItem or Button.
     * @param fxmlPath The resource path to the FXML file for the body content.
     * @param title The title to set for the stage.
     */
    private void navigateToPage(ActionEvent event, String fxmlPath, String title) {
        try {
            // Create a VBox to stack the header, body, and footer
            VBox mainContent = new VBox();

            // Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load header (header.html) using WebView
            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            // Load body (dynamic FXML path)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            // !!!!!!!!!!!! Pass user ID to FrontDisplayProfilesController if applicable
            if (fxmlPath.equals("/MaryemFXML/FrontDisplayProfiles.fxml")) {
                FrontDisplayProfilesController controller = bodyLoader.getController();
                if (controller != null) {
                    Session session = Session.getInstance();
                    controller.setUserId(session.getUserId());
                } else {
                    System.err.println("FrontDisplayProfilesController is null");
                }
            }

            // Load footer (footer.html) using WebView
            WebView footerWebView = new WebView();
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
            URL cssUrl = getClass().getResource("/OumaimaFXML/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Error: styles.css not found in resources at /OumaimaFXML/styles.css");
            }

            URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
            if (userTitlesCssUrl != null) {
                scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
            } else {
                System.err.println("Error: UserTitlesStyle.css not found in resources at /css/UserTitlesStyle.css");
            }

            URL StoreCards = getClass().getResource("/css/store-cards.css");
            if (StoreCards != null) {
                scene.getStylesheets().add(StoreCards.toExternalForm());
            } else {
                System.err.println("Error: UserTitlesStyle.css not found in resources at /css/store-cards.css");
            }

            URL leaderboard = getClass().getResource("/css/leaderboard.css");
            if (leaderboard != null) {
                scene.getStylesheets().add(leaderboard.toExternalForm());
            } else {
                System.err.println("Error: UserTitlesStyle.css not found in resources at /css/store-cards.css");
            }

            // Get the Stage
            Stage stage;
            if (event.getSource() instanceof MenuItem) {
                MenuItem menuItem = (MenuItem) event.getSource();
                MenuButton menuButton = (MenuButton) menuItem.getParentPopup().getOwnerNode();
                stage = (Stage) menuButton.getScene().getWindow();
            } else {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + fxmlPath);
        }
    }

    /**
     * Handles navigation to the Quizzes page (under Learn MenuButton).
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToQuizes(ActionEvent event) {
        navigateToPage(event, "/OumaimaFXML/affichageQuiz.fxml", "Quizzes");
    }

    /**
     * Handles navigation to the Modules page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToModules(ActionEvent event) {
        navigateToPage(event, "/HedyFXML/AffichageModule.fxml", "Modules");
    }

    /**
     * Handles navigation to the Packs page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToPacks(ActionEvent event) {
        navigateToPage(event, "/Boubaker/main.fxml", "Packs");
    }
    @FXML
    private void goToorders(ActionEvent event) {
        navigateToPage(event, "/Boubaker/orders.fxml", "Orders");
    }

    @FXML
    private void goToChatbot(ActionEvent event) {
        navigateToPage(event, "/Boubaker/chatbot.fxml", "Chatbot");
    }

    /**
     * Handles navigation to the My Appointments page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToAppointments(ActionEvent event) {
        navigateToPage(event, "/MaryemFXML/UserConsultations.fxml", "My Appointments");
    }

    /**
     * Handles navigation to the List Doctors page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToListDoctors(ActionEvent event) {
        navigateToPage(event, "/MaryemFXML/FrontDisplayProfiles.fxml", "List Doctors");
    }

    /**
     * Handles navigation to the Store page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToStore(ActionEvent event) {
        navigateToPage(event, "/HamzaFXML/ListStoreItemsFront.fxml", "Store");
    }

    /**
     * Handles navigation to the Leaderboard page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToLeaderboard(ActionEvent event) {
        navigateToPage(event, "/HamzaFXML/Leaderboard.fxml", "Leaderboard");
    }

    /**
     * Handles navigation to the Games Menu page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToGamesMenu(ActionEvent event) {
        navigateToPage(event, "/HamzaFXML/GamesMenu.fxml", "Games Menu");
    }

    /**
     * Handles navigation to the User Titles page.
     */
    @FXML
    @SuppressWarnings("unused")
    private void goToUserTitles(ActionEvent event) {
        navigateToPage(event, "/HamzaFXML/UserTitles.fxml", "User Titles");
    }

    /**
     * Shows the Learn menu programmatically.
     */
    @FXML
    private void showLearnMenu() {
        if (learnMenuButton != null) {
            learnMenuButton.show();
        }
    }

    /**
     * Hides the Learn menu programmatically.
     */
    @FXML
    private void hideLearnMenu() {
        if (learnMenuButton != null) {
            learnMenuButton.hide();
        }
    }

    /**
     * Handles the Home button click.
     */
    @FXML
    private void handleHome(ActionEvent event) {
        System.out.println("Home button clicked");
    }

    /**
     * Handles the Games button click (now handled by MenuButton).
     */
    @FXML
    private void handleGames(ActionEvent event) {
        System.out.println("Games button clicked");
    }
}