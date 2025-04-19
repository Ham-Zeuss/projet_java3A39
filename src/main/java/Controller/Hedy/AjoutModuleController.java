package Controller.Hedy;

import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.ModuleService;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AjoutModuleController {

    @FXML
    private TextField titleField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField nombreCoursField;
    @FXML
    private TextField levelField;

    private final ModuleService moduleService = new ModuleService();

    @FXML
    private void saveModule() {
        try {
            // Get values from form
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String nombreCoursText = nombreCoursField.getText().trim();
            String level = levelField.getText().trim();

            // Validate inputs
            if (title.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
                return; // Stop execution if validation fails
            }

            if (description.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "La description ne peut pas être vide.");
                return;
            }

            int nombreCours;
            try {
                nombreCours = Integer.parseInt(nombreCoursText);
                if (nombreCours <= 0) {
                    showAlert(AlertType.ERROR, "Erreur de saisie", "Le nombre de cours doit être un nombre positif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "Le nombre de cours doit être un nombre valide.");
                return;
            }

            if (level.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "Le niveau ne peut pas être vide.");
                return;
            }

            // Create and save module
            Module module = new Module(title, description, nombreCours, level);
            moduleService.createPst(module);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le module a été ajouté avec succès!");

            // Redirect to affichage page
            redirectToAffichage();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de l'ajout du module.");
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
    private void resetForm() {
        titleField.clear();
        descriptionField.clear();
        nombreCoursField.clear();
        levelField.clear();
    }

        @FXML
        private void redirectToAffichage() {
            try {
                // Recharge la vue tableau
                Parent tableRoot = FXMLLoader.load(getClass().getResource("/HedyFXML/AffichageModule.fxml"));
                Stage stage = (Stage) titleField.getScene().getWindow();
                stage.setScene(new Scene(tableRoot));
                stage.setTitle("Liste des Modules");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}