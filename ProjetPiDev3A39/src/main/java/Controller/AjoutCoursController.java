package Controller;

import entite.Cours;
import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.CoursService;
import java.io.IOException;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


public class AjoutCoursController {

    @FXML private TextField titleField;
    @FXML private TextField pdfNameField;
    private Module currentModule;
    private final CoursService coursService = new CoursService();

    public void setCurrentModule(Module module) {
        this.currentModule = module;
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
            // Create a new Cours object
            Cours newCours = new Cours(title, currentModule, pdfName); // Use the selected PDF file name

            // Save the course to the database
            coursService.createPst(newCours);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été ajouté avec succès!");

            // Return to the AffichageCours screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageCours.fxml"));
            Parent root = loader.load();
            AffichageCoursController controller = loader.getController();
            controller.setModule(currentModule);
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + currentModule.getTitle());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de l'ajout du cours: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageCours.fxml"));
            Parent root = loader.load();
            AffichageCoursController controller = loader.getController();
            controller.setModule(currentModule);
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + currentModule.getTitle());
        } catch (IOException e) {
            System.err.println("Error loading AffichageCours.fxml: " + e.getMessage());
        }
    }
    @FXML
    private void retourCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageCours.fxml"));
            Parent root = loader.load();
            AffichageCoursController controller = loader.getController();
            controller.setModule(currentModule); // Pass the current module to the controller
            Stage stage = (Stage) titleField.getScene().getWindow(); // Get the current stage
            stage.setScene(new Scene(root)); // Set the new scene
            stage.setTitle("Cours: " + currentModule.getTitle()); // Update the window title
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
    // Helper method to show alerts
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait(); // Show the alert and wait for user response
    }
}