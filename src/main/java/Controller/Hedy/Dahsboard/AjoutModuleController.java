package Controller.Hedy.Dahsboard;

import entite.Module;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import service.ModuleService;

public class AjoutModuleController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private TextField nombreCoursField;
    @FXML private TextField levelField;

    private final ModuleService moduleService = new ModuleService();

    // Reference to the popup stage
    private Stage popupStage;

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    @FXML
    private void initialize() {
        nombreCoursField.setText("0");
        nombreCoursField.setDisable(true);
    }

    @FXML
    private void saveModule() {
        try {
            // Get values from form
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String level = levelField.getText().trim();

            // Validate inputs
            if (title.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
                return;
            }

            if (description.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "La description ne peut pas être vide.");
                return;
            }

            if (level.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "Le niveau ne peut pas être vide.");
                return;
            }

            // Set default number of courses to 0
            int nombreCours = 0;

            // Create and save module
            Module module = new Module(title, description, nombreCours, level);
            moduleService.createPst(module);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le module a été ajouté avec succès!");

            // Close the popup
            closePopup();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de l'ajout du module.");
        }
    }

    @FXML
    private void resetForm() {
        titleField.clear();
        descriptionField.clear();
        nombreCoursField.setText("0");
        levelField.clear();
    }

    // Helper method to show alerts
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to close the popup
    private void closePopup() {
        if (popupStage != null) {
            popupStage.close();
        }
    }
}
