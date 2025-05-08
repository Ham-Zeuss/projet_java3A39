package Controller.Hedy;

import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import service.ModuleService;

import javafx.scene.control.Alert;
import javafx.scene.shape.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AffichageModuleController {

    @FXML private GridPane modulesGrid;
    private final ModuleService moduleService = new ModuleService();

    @FXML
    public void initialize() {
        loadModuleCards();
    }

    private void loadModuleCards() {
        modulesGrid.getChildren().clear(); // Clear existing cards
        List<Module> modules = moduleService.readAll();
        int columns = 3;
        int row = 0;
        int column = 0;

        for (Module module : modules) {
            VBox card = createModuleCard(module);
            card.getStyleClass().add("module-card");
            GridPane.setMargin(card, new Insets(10));
            modulesGrid.add(card, column, row);

            column++;
            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    private void showModuleCourses(Module module) {
        try {
            if (module == null) {
                throw new IllegalArgumentException("Module invalide");
            }

            Stage stage = (Stage) modulesGrid.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // 🔐 Check Role Before Loading Navbar
            String userRole = getCurrentUserRole();

            // 1. Conditionally Load header.fxml (Navbar)
            if ("ROLE_PARENT".equals(userRole)) {
                FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                VBox headerFxmlContent = headerLoader.load();
                headerFxmlContent.setPrefSize(1000, 100);
                mainContent.getChildren().add(headerFxmlContent);
            }

            // 2. Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1400);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 3. Load body content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursFront.fxml"));
            Parent bodyContent = loader.load();

            AffichageCoursController coursController = loader.getController();
            coursController.setModule(module); // Pass selected module to controller

            bodyContent.setStyle("-fx-pref-width: 1400; -fx-pref-height: 800; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // 4. Load footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1400);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
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

            Scene scene = new Scene(scrollPane, 1400, 800);

            // Load CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());

            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) scene.getStylesheets().add(navBarCss.toExternalForm());

            URL otherCss = getClass().getResource("/css/affichageprofilefront.css");
            if (otherCss != null) scene.getStylesheets().add(otherCss.toExternalForm());

            URL appointmentsCss = getClass().getResource("/css/appointments.css");
            if (appointmentsCss != null) scene.getStylesheets().add(appointmentsCss.toExternalForm());

            URL gooButtonCss = getClass().getResource("/css/GooButton.css");
            if (gooButtonCss != null) scene.getStylesheets().add(gooButtonCss.toExternalForm());

            URL gamesMenuStylingCss = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStylingCss != null) scene.getStylesheets().add(gamesMenuStylingCss.toExternalForm());

            // Set scene
            stage.setScene(scene);
            stage.setTitle("Cours: " + module.getTitle());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des cours : " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Helper method to get current user role from Session
    private String getCurrentUserRole() {
        return entite.Session.getInstance().getRole();
    }

    private VBox createModuleCard(Module module) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefSize(300, 180);
        card.getStyleClass().add("module-card");

        Label titleLabel = new Label(module.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.getStyleClass().add("heading");

        Label descLabel = new Label(module.getDescription());
        descLabel.setWrapText(true);
        descLabel.getStyleClass().add("para");

        HBox detailsBox = new HBox(10);
        Label countLabel = new Label(module.getNombreCours() + " cours");
        Label levelLabel = new Label("Niveau: " + module.getLevel());
        detailsBox.getChildren().addAll(countLabel, levelLabel);

        card.setOnMouseClicked(e -> showModuleCourses(module));
        card.getChildren().addAll(titleLabel, descLabel, detailsBox);

        return card;
    }

    @FXML
    private void goToAjoutPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/HedyFXML/AjoutModule.fxml"));
            Stage stage = (Stage) modulesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Module");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}