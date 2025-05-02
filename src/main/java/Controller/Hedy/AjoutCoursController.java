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
                String dropboxAccessToken = "sl.u.AFsqQyjH9lgP5DTDgsI9BPfkoyiO9Svjpgeeo_-8fRFIcayZKEGahDevqIf_e03bIBe-TISGNrCA5gAqnrxBJHZCa2k-VzcJGNSjyyMpHAX6diSvRb9LchkK4xqYmCUWZvprA9L60JdDRRWGBkJbIzgQ_enA2d-22h73FtaGBYzHzt62ovFCmq2B1Y8R4hxHV1p_8VO98XxTDhZK-mHcHwYaJjXMCjqdxE4hgrLmeauW0Qhxe1zYR3DZb5Na3w2Vvs8dYw51dmbRrmQOnyAiftIu3NLnTaZR25LnuJ_COefxKY7k6ERLfQzh5Cl20rcTYBrkVLRr8Z21wSHckhNPjESeq-hYmzlX4JaN9ujh5aVMVnh76r76gSOhnF-w-iOYyTnhzIETL5VK-1_6gezvw4x7WDstXDK2MrtXT8akGL0Gz-jbXs4ZLbGqV5rnqKlkIgjeRW9og1viBX95fDBWELiYBRu2LUnpFORMaievngKqTquC6RJDo2AqsMFZxwQRE_K5f0-EvOa5usgXyvJHqWBcK3k0G-amrq0uj1fY1K3Hx3X8X3eeRHCcNsr3ee-dlvMG3A4Th9mrSWix2IQl6NGbGY7J-7pXvhkhPvIn2QnM3UsqKxwd1rS7-Fe30tE6r2n5sHIOUm3_lDVSL8M3mv-nPDWjBJ6bGRvTOMeXCy8lUMQAhL5i6hJu4wUG7pSiYDml4cEuDCQbPPZ6o018E7QiURKc-a31AwPpSFzutWAH4U8G28kiCxgzeGtQNKJDOcjEd_7g8zYnL8uMOXs9MaQ6WWughbDNRWfgbHw4vARlnXtU9pYUnYI4t-VFicwYfW1XicZEppauk9mVjhkFPxHlVWV4JhrpDGRTG2JF6rMEElelcdNcvL2Av-hWOlFx2zzgrNTJlLv5o8Z3AfrFZbzPOuyPmr2hSDQwxbEJWBEQPZqYB4PANuhgNIxiZGYVV_lz56sFjhW_hFljoMCOeLzGCgQs746DkvYNdL8Sg1IiPN01vbm14bMJ0deBGBjmGjwPdSU1GPcFhT4xkIuL9sfriHHwXfmCqPW-PK_8gTEC5bF4xTrdekIVbJG_Ve2AUizkpIT6FQ3ho3h5ncIcSdLcF721LkzYotvJp9YyZl7_PhyzUojkrScpHWHRfFY435QzYuHr09xcyD52osNLwBbUtNbHYdfL8QPT9xC0SiSqiyqM-_eUJDCymoy7GFVj1WxGnfWk-7pfM-XLJtMngfCxI1qD4sQxJWZkmWmhcqrEH6AaV4dTt-eK8eX8Hxkq8tnunkzR-f_vpgahsOErv2bX"; // Replace with your Dropbox access token
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