package Controller.Hedy;

import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.ModuleService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class EditModuleController {
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private TextField nombreCoursField;
    @FXML private TextField levelField;

    private Module moduleToEdit;
    private final ModuleService moduleService = new ModuleService();

    public void setModuleToEdit(Module module) {
        this.moduleToEdit = module;
        titleField.setText(module.getTitle());
        descriptionField.setText(module.getDescription());
        nombreCoursField.setText(String.valueOf(module.getNombreCours()));
        levelField.setText(module.getLevel());
    }

    @FXML
    private void saveChanges() {
        try {
            // Validate inputs
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String nombreCoursText = nombreCoursField.getText().trim();
            String level = levelField.getText().trim();

            if (title.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
                return;
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

            // Update the module object
            moduleToEdit.setTitle(title);
            moduleToEdit.setDescription(description);
            moduleToEdit.setNombreCours(nombreCours);
            moduleToEdit.setLevel(level);

            // Call update service
            moduleService.update(moduleToEdit);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le module a été modifié avec succès!");

            // Return to table view
            returnToTable();

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de la modification du module: " + e.getMessage());
        }
    }


    @FXML
    private void returnToTable() {
        try {
            // Use correct path to your affichagemodule.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/HedyFXML/AffichageModule.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Modules");
        } catch (Exception e) {
            System.out.println("Erreur lors du retour à la liste: " + e.getMessage());
            e.printStackTrace();
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
}