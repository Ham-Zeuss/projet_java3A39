package Controller.Hedy;

import entite.Cours;
import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import service.CoursService;
import java.io.IOException;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import service.ModuleService;

public class EditCoursController {

    @FXML private TextField titleField;
    @FXML private TextField pdfNameField;

    private Cours coursToEdit;
    private final CoursService coursService = new CoursService();

    public void setCoursToEdit(Cours cours) {
        this.coursToEdit = cours;

        // Pre-fill the fields with the existing course data
        if (cours != null) {
            titleField.setText(cours.getTitle());
            pdfNameField.setText(cours.getPdfName());
        }
    }

    @FXML
    private void saveCours() {
        // Validate input fields
        String title = titleField.getText().trim();
        String pdfName = pdfNameField.getText().trim();

        if (title.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
            return;
        }

        if (pdfName.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Aucun fichier PDF sélectionné. Veuillez choisir un fichier PDF.");
            return;
        }

        try {
            // Update the course object
            coursToEdit.setTitle(title);
            coursToEdit.setPdfName(pdfName); // Update the PDF file name
            coursToEdit.setUpdatedAt(java.time.LocalDateTime.now());

            // Save the updated course to the database
            coursService.update(coursToEdit);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été modifié avec succès!");

            // Return to the AffichageCours screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCours.fxml"));
            Parent root = loader.load();
            AffichageCoursController controller = loader.getController();
            controller.setModule(coursToEdit.getModuleId()); // Pass the module back
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + coursToEdit.getModuleId().getTitle());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de la modification du cours: " + e.getMessage());
        }
    }
    // Helper method to show alerts
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait(); // Show the alert and wait for user response
    }
    @FXML
    private void cancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCours.fxml"));
            Parent root = loader.load();
            AffichageCoursController controller = loader.getController();
            controller.setModule(coursToEdit.getModuleId()); // Pass the module back
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + coursToEdit.getModuleId().getTitle());
        } catch (IOException e) {
            System.err.println("Error loading AffichageCours.fxml: " + e.getMessage());
        }
    }
    @FXML
    private void selectPdfFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        Stage stage = (Stage) titleField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Define the target directory for storing PDFs
                File targetDirectory = new File("resources/pdf/");
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs(); // Create the directory if it doesn't exist
                }

                // Copy the selected PDF file to the target directory
                File targetFile = new File(targetDirectory, selectedFile.getName());
                java.nio.file.Files.copy(selectedFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Update the pdfNameField with the new file name
                pdfNameField.setText(targetFile.getName());
            } catch (Exception e) {
                System.err.println("Error selecting or saving PDF: " + e.getMessage());
            }
        }
    }

    public static class AffichageModuleController {

        @FXML private GridPane modulesGrid;
        private final ModuleService moduleService = new ModuleService();

        @FXML
        public void initialize() {
            // Spacing & Padding setup
            modulesGrid.setVgap(15); // Vertical spacing between cards
            modulesGrid.setHgap(0);  // No horizontal spacing
            modulesGrid.setPadding(new Insets(20)); // Padding around grid

            loadModuleCards();
        }

        private void loadModuleCards() {
            modulesGrid.getChildren().clear(); // Clear existing cards
            List<entite.Module> modules = moduleService.readAll();

            int columns = 1; // Only one card per row
            int row = 0;
            int column = 0;

            for (entite.Module module : modules) {
                VBox card = createModuleCard(module);
                GridPane.setMargin(card, new Insets(10, 30, 10, 100)); // top, right, bottom, left
                modulesGrid.add(card, column, row);
                row++; // Move to next row
            }
        }

        private void showModuleCourses(entite.Module module) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCours.fxml"));
                Parent root = loader.load();

                AffichageCoursController controller = loader.getController();
                controller.setModule(module);

                Stage stage = (Stage) modulesGrid.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Cours: " + module.getTitle());
            } catch (IOException e) {
                System.err.println("Error loading AffichageCours.fxml: " + e.getMessage());
            }
        }

        private VBox createModuleCard(Module module) {
            VBox card = new VBox(10);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new Insets(15));
            card.setPrefSize(600, 180);
            card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);
                -fx-cursor: hand;
            """);

            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle("""
                -fx-background-color: #f9f9f9;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);
                -fx-cursor: hand;
            """));
            card.setOnMouseExited(e -> card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);
                -fx-cursor: hand;
            """));

            // Click action
            card.setOnMouseClicked(e -> showModuleCourses(module));

            // Title
            Label titleLabel = new Label(module.getTitle());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            titleLabel.setStyle("-fx-text-fill: #2c3e50;");

            // Description
            Label descLabel = new Label(module.getDescription());
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: #7f8c8d;");

            // Details
            HBox detailsBox = new HBox(20);
            detailsBox.setAlignment(Pos.CENTER_LEFT);
            Label countLabel = new Label(module.getNombreCours() + " cours");
            countLabel.setStyle("-fx-text-fill: #2980b9;");
            Label levelLabel = new Label("Niveau: " + module.getLevel());
            levelLabel.setStyle("-fx-text-fill: #27ae60;");
            detailsBox.getChildren().addAll(countLabel, levelLabel);

            // Assemble
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
}