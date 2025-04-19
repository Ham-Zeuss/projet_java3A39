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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageCours.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageCours.fxml"));
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

}