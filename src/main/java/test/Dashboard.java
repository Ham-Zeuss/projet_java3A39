package test;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class Dashboard extends Application {

    private static final String TEXT_COLOR_DARK = "#333333";

    @Override
    public void start(Stage primaryStage) {
        // Create sidebar
        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                primaryStage,
                () -> loadDashboard(primaryStage), // Dashboard action
                () -> loadFXML(primaryStage, "/User/index_user.fxml"), // Utilisateurs action
                () -> loadFXML(primaryStage, "/HamzaFXML/ListPexelWords.fxml"), // Pixel Words action
                () -> loadFXML(primaryStage, "/User/login.fxml") // Logout action redirects to login
        );

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");
        root.setLeft(sidebar);
        root.setCenter(createMainContent());
        StackPane topBarContainer = createTopBar(primaryStage);
        root.setTop(topBarContainer); // Set the StackPane as the top node
        BorderPane.setMargin(topBarContainer, new Insets(0)); // Ensure no margin around top bar

        // Scene and stage
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Analytics Dashboard");
        primaryStage.show();
    }

    private StackPane createTopBar(Stage stage) {
        // Use a StackPane to overlay the dropdown panels
        StackPane topBarContainer = new StackPane();
        topBarContainer.setStyle("-fx-background-color: #E6E6FA; -fx-border-color: transparent transparent #E0E0E0 transparent; -fx-border-width: 1px;");
        topBarContainer.setMaxHeight(20); // Force max height to 20 pixels
        topBarContainer.setPrefHeight(20); // Set preferred height to 20 pixels

        HBox topBar = new HBox(5); // Reduced spacing for compact layout
        topBar.setPadding(new Insets(2)); // Minimal padding to fit within 20px
        topBar.setAlignment(Pos.CENTER_RIGHT);
        StackPane.setAlignment(topBar, Pos.CENTER_RIGHT); // Align HBox to the right within StackPane

        // Temperature and Flag
        ImageView tempIcon = new ImageView(Sidebar.ICON_CACHE.get("temperature"));
        tempIcon.setFitHeight(14);
        tempIcon.setFitWidth(14);
        Label tempLabel = new Label("26°C");
        tempLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        tempLabel.setTextFill(Color.web(TEXT_COLOR_DARK));

        ImageView flagIcon = new ImageView(Sidebar.ICON_CACHE.get("flag"));
        flagIcon.setFitHeight(14);
        flagIcon.setFitWidth(14);
        HBox.setMargin(flagIcon, new Insets(0, 0, 0, 5));

        // Search Icon
        ImageView searchIcon = new ImageView(Sidebar.ICON_CACHE.get("search"));
        searchIcon.setFitHeight(14);
        searchIcon.setFitWidth(14);
        HBox.setMargin(searchIcon, new Insets(0, 0, 0, 5));

        // Grid Icon
        ImageView gridIcon = new ImageView(Sidebar.ICON_CACHE.get("grid"));
        gridIcon.setFitHeight(14);
        gridIcon.setFitWidth(14);
        HBox.setMargin(gridIcon, new Insets(0, 0, 0, 5));

        // Notification Icon with Badge
        StackPane notificationStack = new StackPane();
        ImageView notificationIcon = new ImageView(Sidebar.ICON_CACHE.get("notification"));
        notificationIcon.setFitHeight(14);
        notificationIcon.setFitWidth(14);
        HBox.setMargin(notificationStack, new Insets(0, 0, 0, 5));

        Circle badge = new Circle(6, Color.web("#FF69B4"));
        Label badgeLabel = new Label("4");
        badgeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        badgeLabel.setTextFill(Color.WHITE);
        notificationStack.getChildren().addAll(notificationIcon, badge, badgeLabel);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(-3, -3, 0, 0));
        StackPane.setMargin(badgeLabel, new Insets(-3, -3, 0, 0));

        // Brightness Icon
        ImageView brightnessIcon = new ImageView(Sidebar.ICON_CACHE.get("brightness"));
        brightnessIcon.setFitHeight(14);
        brightnessIcon.setFitWidth(14);
        HBox.setMargin(brightnessIcon, new Insets(0, 0, 0, 5));

        // Profile Icon and Card
        ImageView profileIcon = new ImageView(Sidebar.ICON_CACHE.get("profile"));
        profileIcon.setFitHeight(16);
        profileIcon.setFitWidth(16);
        HBox.setMargin(profileIcon, new Insets(0, 5, 0, 5));

        VBox profileCard = new VBox(5);
        profileCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #E0E0E0; -fx-border-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        profileCard.setPrefWidth(200); // Reduced width for compact dropdown
        profileCard.setPrefHeight(250); // Reduced height for compact dropdown
        profileCard.setVisible(false);
        StackPane.setAlignment(profileCard, Pos.TOP_RIGHT);
        StackPane.setMargin(profileCard, new Insets(22, 10, 0, 0)); // Adjusted to fit below 20px bar

        HBox profileHeader = new HBox(5);
        ImageView profileImage = new ImageView(Sidebar.ICON_CACHE.get("profile"));
        profileImage.setFitHeight(30);
        profileImage.setFitWidth(30);
        Label profileName = new Label("Laura Monaldo");
        profileName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        profileName.setTextFill(Color.web(TEXT_COLOR_DARK));
        ImageView closeIcon = new ImageView(Sidebar.ICON_CACHE.get("close"));
        closeIcon.setFitHeight(14);
        closeIcon.setFitWidth(14);
        HBox.setMargin(closeIcon, new Insets(0, 0, 0, 20));
        profileHeader.getChildren().addAll(profileImage, profileName, closeIcon);

        Label emailLabel = new Label("lauradesign@gmail.com");
        emailLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        emailLabel.setTextFill(Color.web(TEXT_COLOR_DARK));

        HBox profileDetails = createCardItem("Profile Details", () -> {});
        HBox settings = createCardItem("Settings", () -> {});
        HBox notification = createCardItem("Notification", () -> {});
        HBox incognito = createCardItem("Incognito", () -> {});
        HBox help = createCardItem("Help", () -> {});
        HBox pricing = createCardItem("Pricing", () -> {});
        HBox addAccount = createCardItem("Add account", () -> {});

        HBox logoutBtnCard = new HBox();
        logoutBtnCard.setPadding(new Insets(5));
        logoutBtnCard.setAlignment(Pos.CENTER);
        logoutBtnCard.setStyle("-fx-background-color: #FF69B4; -fx-background-radius: 5;");
        Label logoutLabel = new Label("Log Out");
        logoutLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        logoutLabel.setTextFill(Color.WHITE);
        logoutBtnCard.getChildren().add(logoutLabel);
        logoutBtnCard.setOnMouseClicked(e -> loadFXML(stage, "/User/login.fxml"));

        profileCard.getChildren().addAll(profileHeader, emailLabel, profileDetails, settings, notification, incognito, help, pricing, addAccount, logoutBtnCard);

        profileIcon.setOnMouseClicked(e -> {
            profileCard.setVisible(!profileCard.isVisible());
        });
        closeIcon.setOnMouseClicked(e -> profileCard.setVisible(false));

        // Notification Panel
        VBox notificationPanel = new VBox(5);
        notificationPanel.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #E0E0E0; -fx-border-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        notificationPanel.setPrefWidth(200); // Reduced width for compact dropdown
        notificationPanel.setPrefHeight(80); // Reduced height for compact dropdown
        notificationPanel.setVisible(false);
        StackPane.setAlignment(notificationPanel, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationPanel, new Insets(22, 50, 0, 0)); // Adjusted to fit below 20px bar

        HBox notificationHeader = new HBox(5);
        Label notificationTitle = new Label("Notification");
        notificationTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        notificationTitle.setTextFill(Color.web(TEXT_COLOR_DARK));
        ImageView notificationCloseIcon = new ImageView(Sidebar.ICON_CACHE.get("close"));
        notificationCloseIcon.setFitHeight(14);
        notificationCloseIcon.setFitWidth(14);
        HBox.setMargin(notificationCloseIcon, new Insets(0, 0, 0, 80));
        notificationHeader.getChildren().addAll(notificationTitle, notificationCloseIcon);

        HBox notificationContent = new HBox(5);
        notificationContent.setAlignment(Pos.CENTER);
        ImageView bellIcon = new ImageView(Sidebar.ICON_CACHE.get("notification"));
        bellIcon.setFitHeight(20);
        bellIcon.setFitWidth(20);
        Label notFoundLabel = new Label("Notification Not Found");
        notFoundLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        notFoundLabel.setTextFill(Color.web(TEXT_COLOR_DARK));
        notificationContent.getChildren().addAll(bellIcon, notFoundLabel);

        notificationPanel.getChildren().addAll(notificationHeader, notificationContent);

        notificationStack.setOnMouseClicked(e -> {
            notificationPanel.setVisible(!notificationPanel.isVisible());
            if (notificationPanel.isVisible()) {
                profileCard.setVisible(false);
            }
        });
        notificationCloseIcon.setOnMouseClicked(e -> notificationPanel.setVisible(false));

        // Add elements to topBar
        topBar.getChildren().addAll(tempIcon, tempLabel, flagIcon, searchIcon, gridIcon, notificationStack, brightnessIcon, profileIcon);

        // Add topBar and panels to StackPane
        topBarContainer.getChildren().addAll(topBar, notificationPanel, profileCard);
        return topBarContainer;
    }

    private HBox createCardItem(String text, Runnable action) {
        HBox item = new HBox(5);
        item.setPadding(new Insets(5));
        item.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        label.setTextFill(Color.web(TEXT_COLOR_DARK));
        label.setPrefWidth(160);

        item.getChildren().add(label);

        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 5;");
        });
        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-color: transparent; -fx-background-radius: 5;");
        });
        item.setOnMouseClicked(e -> action.run());

        return item;
    }

    private void loadDashboard(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");

        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                stage,
                () -> loadDashboard(stage),
                () -> loadFXML(stage, "/User/index_user.fxml"),
                () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                () -> loadFXML(stage, "/User/login.fxml")
        );

        root.setLeft(sidebar);
        root.setCenter(createMainContent());
        StackPane topBarContainer = createTopBar(stage);
        root.setTop(topBarContainer);
        BorderPane.setMargin(topBarContainer, new Insets(0));

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        stage.setScene(scene);
    }

    private void loadFXML(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent fxmlContent = loader.load();

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");

            if (fxmlPath.equals("/User/login.fxml")) {
                root.setCenter(fxmlContent);
            } else {
                Sidebar sidebarCreator = new Sidebar();
                ScrollPane sidebar = sidebarCreator.createSidebar(
                        stage,
                        () -> loadDashboard(stage),
                        () -> loadFXML(stage, "/User/index_user.fxml"),
                        () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                        () -> loadFXML(stage, "/User/login.fxml")
                );
                root.setLeft(sidebar);
                root.setCenter(fxmlContent);
                StackPane topBarContainer = createTopBar(stage);
                root.setTop(topBarContainer);
                BorderPane.setMargin(topBarContainer, new Insets(0));
            }

            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlPath + " - " + e.getMessage());
        }
    }

    private VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #F7F7F7;");

        Label headerLabel = new Label("Analytics dashboard");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.BLACK);

        Label subHeaderLabel = new Label("Demographic properties of your customer");
        subHeaderLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subHeaderLabel.setTextFill(Color.web("#666666"));

        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER_LEFT);

        VBox card1 = createCard("All User", "10,234", Color.web("#C20114"));
        VBox card2 = createCard("Event Count", "536", Color.web("#FF8C00"));
        VBox card3 = createCard("Conversations", "21", Color.web("#00FF00"));
        VBox card4 = createCard("New User", "3321", Color.web("#00BFFF"));

        cards.getChildren().addAll(card1, card2, card3, card4);

        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER_LEFT);

        VBox stat1 = createStatBox("Sessions", "6,132", "vs Previous 30 Days");
        VBox stat2 = createStatBox("Page Views", "11,236", "vs Previous 30 Days");
        stats.getChildren().addAll(stat1, stat2);

        content.getChildren().addAll(headerLabel, subHeaderLabel, cards, stats);
        return content;
    }

    private VBox createCard(String title, String value, Color accentColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(150);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#666666"));
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.BLACK);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Circle dot = new Circle(5, accentColor);
        card.getChildren().addAll(titleLabel, valueLabel, dot);
        return card;
    }

    private VBox createStatBox(String title, String value, String subtitle) {
        VBox statBox = new VBox(10);
        statBox.setPadding(new Insets(15));
        statBox.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.setPrefWidth(150);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#666666"));
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.BLACK);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setTextFill(Color.web("#666666"));
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

        statBox.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return statBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}