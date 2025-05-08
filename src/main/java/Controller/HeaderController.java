package Controller;

import Controller.Boubaker.MainBoubakerController;
import Controller.Maryem.FrontDisplayProfilesController;
import entite.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;


import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.stage.Screen;

public class HeaderController {

    @FXML private Button homeButton;
    @FXML private Button learnButton;
    @FXML private Button doctorsButton;
    @FXML private Button gamesButton;
    @FXML private Button packsButton;
    @FXML private ImageView homeIcon;
    @FXML private ImageView learnIcon;
    @FXML private ImageView doctorsIcon;
    @FXML private ImageView gamesIcon;
    @FXML private ImageView packsIcon;
    private MainBoubakerController mainBoubakerController;

    private Popup currentPopup;

    @FXML
    public void initialize() {
        // Load the hover icon image
        Image hoverIcon = new Image(getClass().getResourceAsStream("/Images/menu-hover-icon.png"));

        // Set the image to all icon views
        homeIcon.setImage(hoverIcon);
        learnIcon.setImage(hoverIcon);
        doctorsIcon.setImage(hoverIcon);
        gamesIcon.setImage(hoverIcon);
        packsIcon.setImage(hoverIcon);

        // Set preserve ratio and fit dimensions
        homeIcon.setPreserveRatio(true);
        homeIcon.setFitWidth(20);
        homeIcon.setFitHeight(20);

        learnIcon.setPreserveRatio(true);
        learnIcon.setFitWidth(20);
        learnIcon.setFitHeight(20);

        doctorsIcon.setPreserveRatio(true);
        doctorsIcon.setFitWidth(20);
        doctorsIcon.setFitHeight(20);

        gamesIcon.setPreserveRatio(true);
        gamesIcon.setFitWidth(20);
        gamesIcon.setFitHeight(20);

        packsIcon.setPreserveRatio(true);
        packsIcon.setFitWidth(20);
        packsIcon.setFitHeight(20);
    }

    // Navigation method updated with condition for login page
    private void navigateToPage(Button sourceButton, String fxmlPath, String title) {
        try {
            // Get the stage from the source button
            Stage stage = (Stage) sourceButton.getScene().getWindow();

            // Special case for login page
            if (fxmlPath.equals("User/login.fxml")) {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/" + fxmlPath)));
                Scene scene = new Scene(root, 828, 629); // Match Main.java size
                // Skip initStyle to avoid IllegalStateException
                stage.setScene(scene);
                stage.setTitle(title);
                stage.setResizable(true);
                stage.centerOnScreen();
                stage.show();
                return;
            }

            // Default navigation for other pages
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            // Create a VBox to stack the header, body, and footer
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // 1. Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(width * 0.6, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // 2. Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(width);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 3. Load body content
            Parent bodyContent;
            URL resourceUrl = getClass().getResource("/" + fxmlPath);
            if (resourceUrl == null) {
                System.err.println("Resource not found: /" + fxmlPath);
                return;
            }
            FXMLLoader bodyLoader = new FXMLLoader(resourceUrl);
            bodyContent = bodyLoader.load();

            // Special handling for specific controllers (e.g., FrontDisplayProfilesController)
            if (fxmlPath.equals("MaryemFXML/FrontDisplayProfiles.fxml")) {
                FrontDisplayProfilesController controller = bodyLoader.getController();
                if (controller != null) {
                    Session session = Session.getInstance();
                    controller.setUserId(session.getUserId());
                }
            }
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // 4. Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(width);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Set scene with screen size
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }
            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) {
                scene.getStylesheets().add(affichageprofilefront.toExternalForm());
            }
            URL NavBar = getClass().getResource("/navbar.css");
            if (NavBar != null) {
                scene.getStylesheets().add(NavBar.toExternalForm());
            }
            URL userconsultation = getClass().getResource("/css/appointments.css");
            if (userconsultation != null) {
                scene.getStylesheets().add(userconsultation.toExternalForm());
            }
            URL gooButton = getClass().getResource("/css/GooButton.css");
            if (gooButton != null) {
                scene.getStylesheets().add(gooButton.toExternalForm());
            } else {
                System.err.println("CSS file not found: /css/GooButton.css");
            }
            URL GamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
            if (GamesMenuStyling != null) {
                scene.getStylesheets().add(GamesMenuStyling.toExternalForm());
                System.err.println("CSS loaded: /css/GamesMenuStyling.css");
            } else {
                System.err.println("CSS file not found: /css/GamesMenuStyling.css");
            }

            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading resources for path: " + fxmlPath);
            e.printStackTrace();
        }
    }


    public void setMainBoubakerController(MainBoubakerController controller) {
        this.mainBoubakerController = controller;
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToChatbot() {
        System.out.println("goToChatbot: Checking access for user");
        if (mainBoubakerController == null) {
            System.err.println("goToChatbot: MainBoubakerController is not initialized");
            // Lazy initialization if not injected
            Session session = Session.getInstance();
            if (session.getUserId() > 0) {
                mainBoubakerController = new MainBoubakerController();
                mainBoubakerController.initialize(); // Ensure initialization
                System.out.println("goToChatbot: MainBoubakerController initialized for userId=" + session.getUserId());
            } else {
                showAlert("Erreur", "Impossible de vérifier l'accès au chatbot. Session invalide.");
                return;
            }
        }
        if (mainBoubakerController.hasChatbotAccess()) {
            System.out.println("goToChatbot: Access granted");
            navigateToPage(packsButton, "Boubaker/chatbot.fxml", "ChatBot");
        } else {
            System.out.println("goToChatbot: Access denied");
            showAlert("Accès Refusé", "Vous devez acheter le pack Premium pour accéder au chatbot.");
        }
    }
    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinWidth(400);
        alert.showAndWait();
    }
    // Navigation methods
    @FXML
    @SuppressWarnings("unused")
    private void goToPacks() {
        navigateToPage(packsButton, "Boubaker/main.fxml", "Packs");
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToOrders() {
        navigateToPage(packsButton, "Boubaker/orders.fxml", "Orders");
    }



    @FXML
    @SuppressWarnings("unused")
    private void goToQuizes() {
        navigateToPage(learnButton, "OumaimaFXML/affichageEtudiantQuiz.fxml", "Quizzes");
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToModules() {
        navigateToPage(learnButton, "HedyFXML/AffichageModule.fxml", "Modules");
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToAppointments() {
        navigateToPage(doctorsButton, "MaryemFXML/UserConsultations.fxml", "My Appointments");
    }

    @FXML
    private void goToStore() {
        navigateToPage(gamesButton, "HamzaFXML/ListStoreItemsFront.fxml", "Store");
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToListDoctors() {
        navigateToPage(doctorsButton, "MaryemFXML/FrontDisplayProfiles.fxml", "List Doctors");
    }

    @FXML
    private void goToLeaderboard() {
        navigateToPage(gamesButton, "HamzaFXML/Leaderboard.fxml", "Leaderboard");
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToGamesMenu() {
        navigateToPage(gamesButton, "HamzaFXML/GamesMenu.fxml", "Games Menu");
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToUserTitles() {
        navigateToPage(gamesButton, "HamzaFXML/UserTitles.fxml", "User Titles");
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToHome() {
        navigateToPage(gamesButton, "User/Home.fxml", "Home");
    }

    @FXML
    private void handleHome() {
        System.out.println("Home button clicked");
        navigateToPage(homeButton, "Home.fxml", "Home");
    }

    @FXML
    private void handleGames() {
        System.out.println("Games button clicked");
        // Handled by dropdown, no direct navigation
    }

    @FXML
    private void showLearnMenu() {
        showLearnDropdown();
    }

    @FXML
    private void hideLearnMenu() {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.hide();
            currentPopup = null;
        }
    }

    // Dropdown methods
    @FXML
    private void showHomeDropdown() {
        String[] iconUrls = {
                "https://img.icons8.com/?size=100&id=109681&format=png&color=000000", // Home
                "https://img.icons8.com/?size=100&id=114607&format=png&color=000000"  // Quit
        };

        toggleDropdownWithIcons(homeButton,
                new String[]{"Dashboard", "Disconnect"},
                new String[]{"View your dashboard", "Change your account"},
                iconUrls,
                new Runnable[]{this::goToHome, this::logout}
        );
    }

    private void logout() {
        // Clear the session and navigate to login page
        Session.getInstance().clearSession();
        navigateToPage(homeButton, "User/login.fxml", "Login");
    }

    @FXML
    private void showLearnDropdown() {
        String[] iconUrls = {
                "https://img.icons8.com/?size=100&id=112158&format=png&color=000000", // Quiz
                "https://img.icons8.com/?size=100&id=112289&format=png&color=000000"  // Modules
        };

        toggleDropdownWithIcons(learnButton,
                new String[]{"Quizes", "Modules"},
                new String[]{"Test your knowledge", "Study new topics"},
                iconUrls,
                new Runnable[]{this::goToQuizes, this::goToModules}
        );
    }

    @FXML
    private void showDoctorsDropdown() {
        String[] iconUrls = {
                "https://img.icons8.com/?size=100&id=DzVh6MRkAbVz&format=png&color=000000", // Appointments
                "https://img.icons8.com/?size=100&id=123630&format=png&color=000000"  // Doctors
        };

        toggleDropdownWithIcons(doctorsButton,
                new String[]{"My Appointments", "List Doctors"},
                new String[]{"View your appointments", "Find a doctor"},
                iconUrls,
                new Runnable[]{this::goToAppointments, this::goToListDoctors}
        );
    }

    @FXML
    private void showGamesDropdown() {
        String[] iconUrls = {
                "https://img.icons8.com/?size=100&id=122591&format=png&color=000000", // Store
                "https://img.icons8.com/?size=100&id=111604&format=png&color=000000", // Leaderboard
                "https://img.icons8.com/?size=100&id=111401&format=png&color=000000", // Games
                "https://img.icons8.com/?size=100&id=nUWz0NKN2w_E&format=png&color=000000" // Titles
        };

        toggleDropdownWithIcons(gamesButton,
                new String[]{"Store", "Leaderboard", "Games Menu", "User Titles"},
                new String[]{"Buy games", "Check rankings", "Browse games", "View titles"},
                iconUrls,
                new Runnable[]{this::goToStore, this::goToLeaderboard,
                        this::goToGamesMenu, this::goToUserTitles}
        );
    }

    @FXML
    private void showPacksDropdown() {
        String[] iconUrls = {
                "https://img.icons8.com/?size= Ismail100&id=113642&format=png&color=000000", // Packs
                "https://img.icons8.com/?size=100&id=j11I22jYGwW5&format=png&color=000000", // Orders
                "https://img.icons8.com/?size=100&id=j11I22jYGwW5&format=png&color=000000" // Chatbot
        };

        toggleDropdownWithIcons(packsButton,
                new String[]{"Premium Packs", "Orders", "Chat Bot"},
                new String[]{"Unlock premium features", "See your Orders", "Chat with Our little one"},
                iconUrls,
                new Runnable[]{this::goToPacks, this::goToOrders, this::goToChatbot}
        );
    }

    private void toggleDropdownWithIcons(Button button, String[] titles, String[] subTexts,
                                         String[] iconUrls, Runnable[] actions) {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.hide();
            currentPopup = null;
            return;
        }

        Popup dropdown = new Popup();
        currentPopup = dropdown;

        // Dropdown content with adjusted styling
        VBox menuContent = new VBox();
        menuContent.setPadding(new Insets(15));
        menuContent.setAlignment(Pos.TOP_LEFT);
        menuContent.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 2); " +
                "-fx-min-width: 270;");

        // Add menu items
        for (int i = 0; i < titles.length; i++) {
            HBox item = createMenuItemWithIcon(iconUrls[i], titles[i], subTexts[i], actions[i]);
            menuContent.getChildren().add(item);
        }

        dropdown.getContent().add(menuContent);

        // Precise positioning calculation
        Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());
        Stage stage = (Stage) button.getScene().getWindow();

        // Calculate center alignment accounting for larger content
        double dropdownWidth = 270;
        double dropdownX = stage.getX() + buttonBounds.getMinX() +
                (button.getWidth()/2 - dropdownWidth/2);

        // Position directly below the button
        double dropdownY = stage.getY() + buttonBounds.getMaxY();

        dropdown.show(stage, dropdownX, dropdownY);
        dropdown.setAutoHide(true);

        // Hover behavior
        final boolean[] mouseInButton = {false};
        final boolean[] mouseInDropdown = {false};

        button.setOnMouseEntered(e -> mouseInButton[0] = true);
        button.setOnMouseExited(e -> {
            mouseInButton[0] = false;
            if (!mouseInDropdown[0]) {
                dropdown.hide();
            }
        });

        menuContent.setOnMouseEntered(e -> mouseInDropdown[0] = true);
        menuContent.setOnMouseExited(e -> {
            mouseInDropdown[0] = false;
            if (!mouseInButton[0]) {
                dropdown.hide();
            }
        });
    }

    private HBox createMenuItemWithIcon(String iconUrl, String title, String subText, Runnable action) {
        HBox item = new HBox(15);
        item.getStyleClass().add("app-nav-menu-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 0, 10, 5));

        // Create icon from URL
        ImageView iconView = new ImageView(new Image(iconUrl, true));
        iconView.setFitWidth(50);
        iconView.setFitHeight(50);
        iconView.getStyleClass().add("app-nav-menu-icon");

        // Text content
        VBox textContent = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("app-nav-menu-title");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label subTextLabel = new Label(subText);
        subTextLabel.getStyleClass().add("app-nav-menu-subtext");
        subTextLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));

        // Right arrow
        Label arrow = new Label("→");
        arrow.getStyleClass().add("app-nav-menu-arrow");
        arrow.setFont(Font.font("System", FontWeight.NORMAL, 14));
        arrow.setPadding(new Insets(0, 0, 0, 10));

        textContent.getChildren().addAll(titleLabel, subTextLabel);
        item.getChildren().addAll(iconView, textContent, arrow);

        // Click action
        item.setOnMouseClicked(e -> {
            action.run();
            if (currentPopup != null) {
                currentPopup.hide();
                currentPopup = null;
            }
        });

        return item;
    }

    private void toggleDropdown(Button button, String[] titles, String[] subTexts, String[] icons, Runnable[] actions) {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.hide();
            currentPopup = null;
            return;
        }

        Popup dropdown = new Popup();
        currentPopup = dropdown;

        // Dropdown content
        VBox menuContent = new VBox(10);
        menuContent.setPadding(new Insets(15));
        menuContent.setAlignment(Pos.TOP_LEFT);
        menuContent.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 2); " +
                "-fx-pref-width: 240;");

        // Add menu items
        for (int i = 0; i < titles.length; i++) {
            HBox item = createMenuItem(icons[i], titles[i], subTexts[i], actions[i]);
            menuContent.getChildren().add(item);
        }

        dropdown.getContent().add(menuContent);

        // Get the button's position relative to its scene
        Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());

        // Get the stage (window) position
        Stage stage = (Stage) button.getScene().getWindow();

        // Calculate the dropdown's X position
        double dropdownX = stage.getX() + buttonBounds.getMinX() +
                (button.getWidth()/1 - menuContent.getPrefWidth()/10);

        // Calculate the dropdown's Y position
        double dropdownY = stage.getY() + buttonBounds.getMaxY();

        // Show the dropdown at the calculated position
        dropdown.show(stage, dropdownX, dropdownY);

        // Auto-hide and hover behavior
        dropdown.setAutoHide(true);

        final boolean[] mouseInButton = {false};
        final boolean[] mouseInDropdown = {false};

        button.setOnMouseEntered(e -> mouseInButton[0] = true);
        button.setOnMouseExited(e -> {
            mouseInButton[0] = false;
            if (!mouseInDropdown[0]) {
                dropdown.hide();
            }
        });

        menuContent.setOnMouseEntered(e -> mouseInDropdown[0] = true);
        menuContent.setOnMouseExited(e -> {
            mouseInDropdown[0] = false;
            if (!mouseInButton[0]) {
                dropdown.hide();
            }
        });
    }

    private HBox createMenuItem(String icon, String title, String subText, Runnable action) {
        HBox item = new HBox(10);
        item.getStyleClass().add("app-nav-menu-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(5, 0, 5, 0));

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("app-nav-menu-icon");
        iconLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

        // Text content
        VBox textContent = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("app-nav-menu-title");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label subTextLabel = new Label(subText);
        subTextLabel.getStyleClass().add("app-nav-menu-subtext");
        subTextLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        // Right arrow
        Label arrow = new Label("→");
        arrow.getStyleClass().add("app-nav-menu-arrow");
        arrow.setFont(Font.font("System", FontWeight.NORMAL, 12));
        arrow.setPadding(new Insets(0, 0, 0, 5));

        textContent.getChildren().addAll(titleLabel, subTextLabel);
        item.getChildren().addAll(iconLabel, textContent, arrow);

        // Click action
        item.setOnMouseClicked(e -> {
            action.run();
            if (currentPopup != null) {
                currentPopup.hide();
                currentPopup = null;
            }
        });

        return item;
    }
}