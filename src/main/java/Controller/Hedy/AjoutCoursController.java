package Controller.Hedy;
import Controller.Hedy.Dahsboard.*;

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
        // Validate input fields
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
            // Ensure the current user's ID is set
            if (currentUserId == null) {
                showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer l'utilisateur actuel.");
                return;
            }

            // Create a new Cours object with the user's ID and the PDF URL
            Cours newCours = new Cours(
                    title,
                    currentModule,
                    pdfUrl, // Save the public URL of the PDF
                    currentUserId // Store the user's ID
            );

            // Save the course to the database
            coursService.createPst(newCours);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été ajouté avec succès!");

            // Return to the AffichageCours screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
            Parent root = loader.load();

            // Use the correct controller class
            AffichageCoursDashboardHedy controller = loader.getController(); // Correct controller
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
                String dropboxAccessToken = "sl.u.AFtbinby3m22XcRR7qkRjB3rAd8GbjcJa7fHGSYUa3uFumv32WIPfcxtQxzyZ7eV5xTFGRVx8wdeK4NDkJy3y8g_mMyyUCyVWGpIamHlg29V24C1ns91aoTgozZtHCyEZgy2lVBYqSWogCqpCJ5HbqpkFxZ2zeYlXIwKoliQzqyjenVCX2XK8w2jzUHZyQW68F39ZOkIBx1C6slDTsVdz_cB9JuhswKrAhqBAOn0OI0lTlAj2VTBZuAgUhBBwKImVfcWdUyHrGwpsZqOZoBOm32YMFAZ1q6AIoj1wpTpWD9Puiy65R9jHfg90umjKVL-YDFTIWZ1Pmag5cQKBr_4HrpUCq_8KLOvijVgfGTvxbMN2qmhaaRdRd1o8V1HTCDkKkJdyGOUpu0GPl18VotkRhWfnVxEpHNRLPpn7hLtDWzJvjsADzS-54ADG00GhbEs__9vKjAC08GT0AxFTAOTjx8eF3F78BbVyTsgwV-t-CHGKk9f0cPmovMbVDbmRLLAi1I2aEIt5hRKoddTkxwRVIlkXRu--qEORLIOy49_-nqE2PIdCKqVepF_yGP4zmn98x-YzHwtvHTbp0vaFU55TNw2f1Y2huXBXU0988HlW47ccd9mmsTN0-TKdWqS-fSIkH6W5I4EL1krTE85OmrOGGbhROz0GNp1ym1x6v8-x6MUfK7HUIyGPMg1AgPqBXPnGfyqoAqRgyXyAIWyqIEN4Ob2PSsUEYwf9W3MI8sf0Fo8Ee5R1kH4IzulLOao9DfioVteziO4wstWnLNCwuiE-m2GuAx1FcZxUfsmpA2AvpKB2RLMOeq1bU-v-_3HXSvmKrg0JGqbIUde-6MvSOsEDUgenFZXCMzaQZzBkeTT9xN8I4-8PuAzbcWLRQpp974nm5dFKUUQVFQrBMRXTS_yv76i9H8ioK3kOPIK3nI2swOVzF5Ex78GyyswXMjXtesbsgK8zGfvuLI64EAiNUWkhQ_AxbY83AZyGrVGdmqfgaOYdoC9EQvV53wxo3CKkJVngNc2wlgtGUxJCjnQPx3DHyD7fqGYJE2dujZFAkdGHbMZqQ6FqXQec5mPTzIKhaJoQucEZDb4rkBQeRp2AvgERrgGmAFPW10VS_15G136hdYo8qvZJEk0zt8OWtST-xeaPruWmMmf3voAWGldp4e7lRPw7NFAwDOh5sdHAzrOHiv1QFKobDjYW71GA0I7mKDcriWR29DN8diiAj4cOp5NbgUsua-qNOeWcrsEV9vfXarxP3zxek64K51WNNR8tgshYPIvjBpXsmdgldh4f-Rnws_p"; // Replace with your Dropbox access token
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