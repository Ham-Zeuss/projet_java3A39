package Controller.Hedy;

import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import service.ModuleService;

import java.io.IOException;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursFront.fxml"));
            Parent root = loader.load();

            // Pass the selected module to the AffichageCoursController
            AffichageCoursController controller = loader.getController();
            controller.setModule(module);

            // Update the stage with the new scene
            Stage stage = (Stage) modulesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + module.getTitle());
        } catch (IOException e) {
            System.err.println("Error loading AffichageCoursFront.fxml: " + e.getMessage());
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