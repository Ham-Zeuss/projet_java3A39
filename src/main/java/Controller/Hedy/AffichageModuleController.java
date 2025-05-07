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
            Stage stage = (Stage) modulesGrid.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load Header Image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1500);
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

            // Load the Cours Page (Body)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursFront.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("Fichier /HedyFXML/AffichageCoursFront.fxml introuvable");
            }

            Parent bodyContent = loader.load();
            AffichageCoursController coursController = loader.getController();
            coursController.setModule(module); // Pass selected module to controller

            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // Load Footer Image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1500);
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

            // Create Scene
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Load CSS
            URL cssUrl = getClass().getResource("/css/your-cours-style.css"); // Replace with actual CSS path
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS file not found: /css/your-cours-style.css");
            }

            // Set Scene on Stage
            stage.setScene(scene);
            stage.setTitle("Cours: " + module.getTitle());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des cours : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private VBox createModuleCard(Module module) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefSize(300, 180);

        // ✅ Apply CSS class only — no inline styles!
        card.getStyleClass().add("module-card");

        // Title
        Label titleLabel = new Label(module.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.getStyleClass().add("heading"); // Use .heading from CSS

        // Description
        Label descLabel = new Label(module.getDescription());
        descLabel.setWrapText(true);
        descLabel.getStyleClass().add("para"); // Use .para from CSS

        // Details
        HBox detailsBox = new HBox(10);
        Label countLabel = new Label(module.getNombreCours() + " cours");
        Label levelLabel = new Label("Niveau: " + module.getLevel());
        detailsBox.getChildren().addAll(countLabel, levelLabel);

        // Click handler
        card.setOnMouseClicked(e -> showModuleCourses(module));

        // Add all components to card
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