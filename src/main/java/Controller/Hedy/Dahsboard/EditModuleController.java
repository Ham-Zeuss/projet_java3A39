
package Controller.Hedy.Dahsboard;


import entite.Module;
import javafx.fxml.FXML;
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

    // Method to set the module to edit
    public void setModuleToEdit(Module module) {
        this.moduleToEdit = module;
        if (module != null) {
            titleField.setText(module.getTitle());
            descriptionField.setText(module.getDescription());
            nombreCoursField.setText(String.valueOf(module.getNombreCours()));
            levelField.setText(module.getLevel());

            // Disable the 'nombreCoursField' as it should not be edited
            nombreCoursField.setDisable(true); // Disable the field
        }
    }

    @FXML
    private void saveChanges() {
        try {
            // Validate inputs (skip validation for nombreCoursField)
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String level = levelField.getText().trim();

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

            // Get nombreCours directly from moduleToEdit (no need to parse it)
            int nombreCours = moduleToEdit.getNombreCours(); // We use the original value

            // Update the module object
            moduleToEdit.setTitle(title);
            moduleToEdit.setDescription(description);
            moduleToEdit.setNombreCours(nombreCours);
            moduleToEdit.setLevel(level);

            // Call update service
            moduleService.update(moduleToEdit);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le module a été modifié avec succès!");

            // Close the popup
            closePopup();

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de la modification du module: " + e.getMessage());
        }
    }

    @FXML

    private void cancel() {
        closePopup();

    }

    // Helper method to close the popup
    private void closePopup() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
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
