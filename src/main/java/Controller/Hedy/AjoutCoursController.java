package Controller.Hedy;
import Controller.Hedy.Dahsboard.*;
import service.ModuleService;
import entite.Cours;
import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.CoursService;
import service.DropboxService;
import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AjoutCoursController {

    @FXML private TextField titleField;
    @FXML private TextField pdfNameField;
    private Module currentModule;
    private final CoursService coursService = new CoursService();
    private Integer currentUserId; // Store the ID of the currently logged-in user

    public void setCurrentModule(Module module) {
        this.currentModule = module;
    }

    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
        System.out.println("Current User ID set to: " + currentUserId); // Debugging log
    }

    @FXML
    private void saveCours() {
        String title = titleField.getText().trim();
        String pdfUrl = pdfNameField.getText().trim();

        if (title.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
            return;
        }

        if (pdfUrl.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Aucun fichier PDF sélectionné. Veuillez choisir un fichier PDF.");
            return;
        }

        try {
            if (currentUserId == null) {
                showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer l'utilisateur actuel.");
                return;
            }

            Cours newCours = new Cours(
                    title,
                    currentModule,
                    pdfUrl,
                    currentUserId
            );

            coursService.createPst(newCours);

            // 👇 Increment module's course count and update DB
            ModuleService moduleService = new ModuleService();
            currentModule.setNombreCours(currentModule.getNombreCours() + 1);
            moduleService.update(currentModule); // Make sure this method exists!

            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été ajouté avec succès!");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
            Parent root = loader.load();

            AffichageCoursDashboardHedy controller = loader.getController();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
            Parent root = loader.load();

            // Use the correct controller class
            AffichageCoursDashboardHedy controller = loader.getController(); // Correct controller
            controller.setModule(currentModule);

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + currentModule.getTitle());
        } catch (IOException e) {
            System.err.println("Error loading AffichageCoursDashboard.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void retourCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
            Parent root = loader.load();

            // Use the correct controller class
            AffichageCoursDashboardHedy controller = loader.getController(); // Correct controller
            controller.setModule(currentModule); // Pass the current module to the controller

            Stage stage = (Stage) titleField.getScene().getWindow(); // Get the current stage
            stage.setScene(new Scene(root)); // Set the new scene
            stage.setTitle("Cours: " + currentModule.getTitle()); // Update the window title
        } catch (IOException e) {
            System.err.println("Error loading AffichageCoursDashboard.fxml: " + e.getMessage());
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
                // Initialize Dropbox Service
                String dropboxAccessToken = "sl.u.AFs7qV8GBEh-LQ2nMtSvs9JipnXS9eF8cmTkqdgOsrpbNfPZD6wh8MUzKza633w9484zX9-EZfGcPJtxMTvsw98AxAKUlR9mpv6cd2g-_OJYBhxbDBQgY2lniqFdtgyMa6LIm6NANChQYY3Xlo3QpzyC1BD7O6CJwubUA8SbTKbTyulwsIpcYz_JlmlZXphrY_U5q2Hnuh1zY447uSA3lU5rt5x80i1S-tbBjEid4a2KVLxGhEuZJm0ti9qtUMFGNGWWJti9eu9wy9ilpumTPBhIFgniygTLDn_I4VfBO99Zy70calPIgsS7qRXnkPBX4vYtdsbISbiTiAlsUD8ia-jCTUIZU-hKTt7HWP-5DMIifDONJZxEO0RzmmtVrb5V12VkX62IxOqYw7NEhHgpc_HSyr3kgRoyLUdQlmHZPth0i_3hcAWk7FGvLKJGnd-Gk8pyYHQgxcZH-VwUiGizjDG6-ISJ7eQGSNDQzhnWUQKG-LKHC1G2HaxbVdEdupAnrcNvGmuTwTojv9iyjoD5pW2YuxLvkEIUUQrlpzqwEf-B1CEjwtNqcnNgQNyDDm3i8zNLmM_ojPlLcx-fcpDgUV4g4TKMs_bC7szq5zsU0vxcLYxRTLCxsx5wzQOwBCF4l-zOODENzquIk4mWhY-zPOr22pdKOHvxEgrY6xn9RR49DUEZwLrAQ5EYZVEe8beCh-ifX43POygNyeJiToDEkBbNBdU6P18SDE2zQBLYsAeJ8_8x4q1wa7TjEgxR-s81SBWTwF7TQR_O1McklMBszKOG6BWPjhiIdqy9e4ST791O-XGCnnoJcK7AaVyFK1ZvWUOSTc-5_T9rPLSvm4mWqoBEWKdfe8s8at_EuKQEeSo5feGP3fLmK30ZgaQ4Bz2WNcgdVKYqTqYbJqsCNnK4vsTxWaGCMhvKMczvdWfAkDaa0ruBlf9Zb7ZobUcthDvGij-VhEFHzUws4j3P-WHtgEdETehJ5MYwgp9aPjgdOal5dDv0gLwyuee4NxKaAWzblihvd3sXwCiB0JeBmXsO3UplowPjGooCSOYGuD_LWyLgn0d1lYXaLKWhA48boxRXKF8iJclX7o2033z0fHc_KgpVIUfzwp8-92QXBFBm0BMxfcN4yeMSD27Lj-ffj8MOpzBJ_YolHIQyNQpP25Ce5XHzNJW1eO7dvhA8F2m653Mbdg7-jnQGd1F_sasHKr85jyjvFaKaxZP0v-XAuR1YSuMWlwKqUv5penAnQgMSFmZV_DXI3Oy1r_0xxh4XrMBd7KAKsmwSvqn116Oe-Gu40dPJ"; // Replace with your Dropbox access token
                DropboxService dropboxService = new DropboxService(dropboxAccessToken);

                // Define the destination path in Dropbox
                String dropboxPath = "/pdfs/" + selectedFile.getName(); // Destination path in Dropbox

                // Upload the selected PDF file to Dropbox
                dropboxService.uploadFile(selectedFile.getAbsolutePath(), dropboxPath);

                // Retrieve the public URL of the uploaded file
                String fileUrl = dropboxService.getFileUrl(dropboxPath);

                if (fileUrl == null) {
                    showAlert(AlertType.ERROR, "Erreur d'upload", "Impossible de récupérer l'URL du fichier.");
                    return;
                }

                // Update the pdfNameField with the public URL of the uploaded file
                pdfNameField.setText(fileUrl);

                // Show success message
                showAlert(AlertType.INFORMATION, "Succès", "Le fichier PDF a été uploadé avec succès!");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur d'upload", "Une erreur s'est produite lors de l'upload du fichier PDF: " + e.getMessage());
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