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
    private Module currentModule; // To hold the loaded or newly created module

    private Stage popupStage;

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    @FXML
    private void initialize() {
        nombreCoursField.setText("0");
        nombreCoursField.setDisable(true);
    }

    // Load an existing module (for editing)
    public void setModule(Module module) {
        this.currentModule = module;
        titleField.setText(module.getTitle());
        descriptionField.setText(module.getDescription());
        nombreCoursField.setText(String.valueOf(module.getNombreCours()));
        levelField.setText(module.getLevel());
    }

    public Module getModule() {
        if (currentModule == null) {
            currentModule = new Module();
        }
        currentModule.setTitle(titleField.getText().trim());
        currentModule.setDescription(descriptionField.getText().trim());
        currentModule.setLevel(levelField.getText().trim());
        currentModule.setNombreCours(Integer.parseInt(nombreCoursField.getText()));
        return currentModule;
    }

    @FXML
    private void saveModule() {
        try {
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

            Module module = getModule();
            if (module.getId() == 0) {
                // New module
                module.setNombreCours(0); // Default
                moduleService.createPst(module);
            } else {
                // Existing module, just update
                moduleService.update(module);
            }

            showAlert(AlertType.INFORMATION, "Succès", "Le module a été sauvegardé avec succès!");
            closePopup();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de l'ajout du module.");
        }
    }

    // Increment number of courses
    public void incrementNombreCours() {
        int current = Integer.parseInt(nombreCoursField.getText());
        current++;
        nombreCoursField.setText(String.valueOf(current));
        currentModule.setNombreCours(current);
        moduleService.update(currentModule); // Save updated value
    }

    // Decrement number of courses
    @FXML
    private void decrementNombreCours() {
        int current = Integer.parseInt(nombreCoursField.getText());
        if (current > 0) {
            current--;
            nombreCoursField.setText(String.valueOf(current));
            currentModule.setNombreCours(current);

            // Call dedicated decrement method instead of full update
            moduleService.decrementNombreCours(currentModule.getId());

            // Confirm the decrement happened
            System.out.println("Decrementing course count for module ID: " + currentModule.getId());
        } else {
            System.out.println("Cannot decrement below zero.");
        }
    }

    @FXML
    private void resetForm() {
        titleField.clear();
        descriptionField.clear();
        nombreCoursField.setText("0");
        levelField.clear();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closePopup() {
        if (popupStage != null) {
            popupStage.close();
        }
    }
}