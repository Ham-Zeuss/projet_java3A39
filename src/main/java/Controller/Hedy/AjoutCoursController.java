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
                String dropboxAccessToken = "sl.u.AFu7xqHVBs-mi1HPr0QNYNBAkGS4-kM5k3WCdBblu8KO8zu1mi4mKwx-duwRYb-Eb_sCgPyW2mc_IRuGk0WP1morkpp9b5jH2UKkGTb4q36bE2Lgjl7UTFIp1cKaEvAySeML_6gYK9gOPG3RzP1QC6mSP_rLWlFXRcwFkqfnh0iOfxIRYAaE4PwJdaiZfPIYGJVZuqef_mCb3FJIviVn6GE5DUb6yyQx2GrC7s0t_wmJDxFSO8v5Ny4VbHElrRNtvNrzlURgs4kZTG53WFKM8RMhLaKh_oJEOJgxyOwSZxgdzBjuKoAk9juRSbNPYASWS8Qhu2T3u9mCd7fRP4fUmw1vZY1NIea8wbqZROLCIZgwiAzHBtGVzP0QYClINKBuehseNyc5lkWsYTu3ItiDPM5vZ2nGRW6fMJ-M6n3IIDI3E7GPcYh0Lxzrfi42NxfgFgOek9tbrYp5BX___-fGgK24Ih2cbiqoXLS_ufUS9dE7G2xokRCZ0_tQS1d0CRwjORsRyIeyk7-4hECIq-dBREKqNhKV-SIPQSJnw39nJ0dvAJobfvMRtjk15Qnzvbgvd6rfsknjaAJdEBpNSaDdF7icEa2dVgnkulHXdTquzGjMyFGC1NHA8zFFeNFuUCWA5uANHz6FTMwbiCXEdJ1WrTC0dxFQsk7C71EP_wVHcqES7Y14AKpplTnXUs8OPRl9897xOweiBUBrxlcxbwNiGK1OxzWqfcw_bLSSBNRITc5kZS6VTG0mqwOkb60_NJIYlLFdeS2EsAlbGdKilzt9WN7Ivs6-3kApxTpZyqkE4qEaJFtLP6tCLrxK4TgB93qDaAD-pxW7iD9r4XMH8RQwiqX6J_EXlxCgvKI5g_zvaQaAD4gVvO0twpulq8Xf4sPqDjlpvZBTP4vnksjQkbqbjyicVoEIc3EBIMzZZ7lrxNyRil2MOccmbY3RWYYvdX4fAN7qjZdPk0MDsSO85hBGQANTv63QBIU7LbFTkt4_8rsjn2QszvLeBa1E_8TZCVbWZ35e4ZEJAT-s0s5BP6NgrD5eJNp6n3dK0tPVc5Sjz4oaqOZCixASfixIAP3MwcedimFZkxRk016Rs0By9wKkqqFybjm0cORoYneMn-S_2HgCuo38skQNUyhW6uW96oxzrlI0Lrkirdu6Fk-znDoTNTiHDrd2-WYdR9IrVAZABaUPhS6uP0f5edOXomhb8aEfgekgqut5MtKmJgsvjAz6RTFBJng4jXhWSAHvPWS9ntbVHQqm8rYHandu-bzIaRGI7WrLlBKOYTpaFtTrfs2vjIfB"; // Replace with your Dropbox access token
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