package test;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

    // Static variable to simulate the logged-in user's ID
    public static int loggedInUserId = 22; // Set the user ID to 22 for testing

    @Override
    public void start(Stage primaryStage) {
        // Create sidebar
        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                primaryStage,
                () -> loadDashboard(primaryStage), // Dashboard action
                () -> loadFXML(primaryStage, "/User/index_user.fxml"), // Utilisateurs action
                () -> loadFXML(primaryStage, "/HamzaFXML/ListPexelWords.fxml"), // Pixel Words action
                () -> System.out.println("Logout clicked (implement logout logic here)"),// Logout action
                (fxmlPath) -> loadFXML(primaryStage, fxmlPath)
        );

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");
        root.setLeft(sidebar);
        root.setCenter(createMainContent());

        // Scene and stage
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Analytics Dashboard");
        primaryStage.show();
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
                () -> System.out.println("Logout clicked (implement logout logic here)"),
                (fxmlPath) -> loadFXML(stage, fxmlPath)
        );

        root.setLeft(sidebar);
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        stage.setScene(scene);
    }

    private void loadFXML(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent fxmlContent = loader.load(); // Load as Parent, not BorderPane

            // Pass the logged-in user ID to the controller if applicable
            Object controller = loader.getController();
            if (controller instanceof Controller.Hedy.AjoutCoursController) {
                ((Controller.Hedy.AjoutCoursController) controller).setCurrentUserId(loggedInUserId);
            } else if (controller instanceof Controller.Hedy.Dahsboard.AffichageModuleDashboardController) {
                ((Controller.Hedy.Dahsboard.AffichageModuleDashboardController) controller).setLoggedInUserId(loggedInUserId);
            }

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");

            Sidebar sidebarCreator = new Sidebar();
            ScrollPane sidebar = sidebarCreator.createSidebar(
                    stage,
                    () -> loadDashboard(stage),
                    () -> loadFXML(stage, "/User/index_user.fxml"),
                    () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                    () -> System.out.println("Logout clicked (implement logout logic here)"),
                    (fxmlPathInner) -> loadFXML(stage, fxmlPathInner)
            );

            root.setLeft(sidebar);
            root.setCenter(fxmlContent); // Set the loaded FXML content as center

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

        // Header
        Label headerLabel = new Label("Analytics dashboard");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.BLACK);

        // Subheader
        Label subHeaderLabel = new Label("Demographic properties of your customer");
        subHeaderLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subHeaderLabel.setTextFill(Color.web("#666666"));

        // Cards
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER_LEFT);

        VBox card1 = createCard("All User", "10,234", Color.web("#C20114"));
        VBox card2 = createCard("Event Count", "536", Color.web("#FF8C00"));
        VBox card3 = createCard("Conversations", "21", Color.web("#00FF00"));
        VBox card4 = createCard("New User", "3321", Color.web("#00BFFF"));

        cards.getChildren().addAll(card1, card2, card3, card4);

        // Stats
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