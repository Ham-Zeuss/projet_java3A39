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
                String dropboxAccessToken = "sl.u.AFswQNc9Py4NNONbvPJBke9NvJnfa_-7W5x3K-TMACnReyhGTflOrdQF1SC0LpC5gHXuCOUQ6MugX20AZVGq24ZW7V2HwWzGFaRK8UFiMAgQE6WBeOA9irJRJWu2phQHdvB9eQ8mhbCUXlEvd5k1DqNDZx3-n2dEh0xQozIDncV7QNx19AGn0LoHKYS_vYiPGzYl-lC3Jvo0nP8dU6mmQDfpKEB_jDIpMUXDj-en0xRTYVxRCw35ROiMKbVz-pznI4YxWFm94XSfdNeAOXx7DPSvWDa6zIjgt_B7N9OEK5IJxzO8i6uIKNUJdef7LNY5kDGZCgHDQBKpQs5YchbLE2Oq09-SFpPio2hK9QhTFxzt2gBB-I1yLZD5tLlwYtRUps2Nkhx6ykVfPcQTbXIMyxFM15Vu6ArwBxiYcs5-_IjBJ55H5Ozrz7XIClnhmL-tNRR4FhtCdkWuJ8O6Wqm2QfxlbQRHOaNix6OGa5bqERoUcMNwvFR-cdZQW-dG7WUXARAgXyqkVwCXFaqTmYnaq-dA6fTG1Mk-1V-rgOIRaf40550VOJiHWJOlhZT1wMZo63j1FuEtACFHrFL_suOuGSpc83wwp5JRAv5LztDfv7vuoXc14uQJSRQt8S1mXcEg5IrtDct34ncGG0kq1LexFPYtlsnIkGb6Qz_kwnHWAeZifD18kdfjulwKH9eCWBznM-A4jYBQ1QCLfqMrXLvpKJLgoZOCRCl9vAg-aL4iZYubdJHf5C9iMPCE8V0Y0aUrLPAmIyxAeLJhhS0aFFOqT3nTirXG2iOMAJ_Xh4i8e4T5DFa7cKEjE-H_4cOCWOT-S_hNmTYe0Vb4iJqap9Qv9Ie3gJ5F6htUxr-9pf9Xr6kb9Mq3rFLWPHfOSmsWdi4aBM_Qfrt-YwBRINRejgdJgbBe0yWtLhmenT9bN7AS_qoLQKtqE7ACN42Odp0-QO2YvQeLP3lPq9mWYWkt3uZ7hGw1gTOOtRTslhICpcycFlus03Jh5cJe1H9TVrdVkj62tNMA5w8wpYJ2aoKlRnFK9X4fQJJDoM9DT7DYBaeeY8lflyoYh3dfW22OQ4zU_Q36xSGquX_RTny1N_hf12I_j1inZ-cl3svJnnAUpUsiwRbJ3Cm__k1vKCSzQ3_sW5dFh01JWFp6fXeAvYLmn7T7SrkgimbYQbX-NLaBfI8FsVMwn4NkiMJdiYJKJCehB10ILiv96j8QWKSwJNEHpm0BigES1gg96g7ga4MK7SyoggWcObjRGNT8LhTLwXZTtiM4KC4eKMnOxw7fNqAo1xTN-oCi"; // Replace with your Dropbox access token
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