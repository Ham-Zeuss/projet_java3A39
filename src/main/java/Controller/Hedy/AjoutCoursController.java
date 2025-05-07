package Controller.Hedy;
import Controller.Hedy.Dahsboard.*;
import entite.Session;
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
    Session session = Session.getInstance();
    String userRole = session.getRole();
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

            // 🔁 Redirect based on user role
            Session session = Session.getInstance();
            String userRole = session.getRole(); // Fetch role from session

            String fxmlPath;
            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                fxmlPath = "/HedyFXML/AffichageCoursFront.fxml";
            } else if ("ROLE_ADMIN".equals(userRole)) {
                fxmlPath = "/HedyFXML/AffichageCoursDashboard.fxml";
            } else {
                showAlert(AlertType.ERROR, "Accès refusé", "Vous n'avez pas les droits nécessaires pour accéder à cette page.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Set the module in the target controller if applicable
            Object controller = loader.getController();
            if (controller instanceof AffichageCoursDashboardHedy) {
                ((AffichageCoursDashboardHedy) controller).setModule(currentModule);
            } else if (controller instanceof AffichageCoursController) {
                ((AffichageCoursController) controller).setModule(currentModule);
            }

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
                String dropboxAccessToken = "sl.u.AFuEdDTepz_XWGcMk9LRysH6u9FIJ4Esd-NA7mezoAdBTo1pm5UOtMbB7OOY-xeW_g1Z5U8-Z5x6Tg_6z-3Eu8j0Od3b_ggy4HklyJw-u47qbeG97c_NYdFbzGd4AUXUlQni1JbWl-SOyV42uVdYun4jGW4W1EQbZoAyLNs8LTzpD2Aj_wekr5goTMkgBYUrvnajvH7K8bXjtMmKeIq5czwJ-eFdXMLoqs3C8T-7qW_K2cctF6_J-VkKGT3vIqmS9eKRAjfvhz_DNh3PSpRbFHYYGN2B9mkh_6KFZSyNea51JBC8nydaM_h5RktPx9IQr2stxLP5imi0z_pdYJnNboRuYdGK-goXxQ_Xa59Oxbb566hgl3dyRRgJmIbBKm0QUq-NBctwHzfRmhfYnKb1enq9YtkerX4iuf-H_n2Ph4F9XdHPLcM-Tmlay7QuTVQfaxAH4g1mic0mG0RnY-5zZ0cla5QomO2MqOxrez2EqnVYUUyLrj1O6lR2bY2EroVQgp3P8HA6fv9SfcWhl1oylmnwaKerzm_xrYxLHPgauhAGbqATt0tY_cnGFYgiTjCAwlzFVlS1-9TnFckFEBhuMTUCM2KCMd62jwmsJ_OKP5JNbs6-TsgQq-2KnDWEWbJAy2B2o-d6TcHupFK_6vypnfxguxWLFpRgEMXDtoLACxQta63n4aDcXFMxdCkSshVPV5LrOShkbQfNa8nFj3qNtjFwyrdiVwZIGrY2aUu5CbbwsMM6Krf6dI377WQo3BxvQwbTuyZw_tH94FXfcxvwZdsG7Sp3ZjfXsr6aBGVjl4ycuPbuz8IxB-HYqCKiUqnuwfMZcZeaAnjZr30sKO4GhUeJi43XobL4pBbkrEBpjAE4JS-eBmY2Bt05AAjMCfLbsptkclEce8_-fCnKG5wD-8Vo5ZQ6b-4JMOd5TJjL2_yk4YXoqQFD_EuKlEmF1TMK5pTrC-c6U5CPNLwpRpwMZRznFtWtHUqyNSWtXhkWgGWcElrYJGMUayF9N0c4ubIS7DwKZO_wjZCQqifYj4DjGJXbXxF4oPNSsOECkDmHubeRmcY_MBWrqh1AZnpIoh6hTisKJuO53OaD8-9FPt_AuwEm9bECbMpCy9byoBRVZDWFTBXvl2SAUSuw9P11iBClGwi3Yw9jnVtKdUJZnR4Vixlj7AgT42ggKIVS3wDmibqn77ShydB4SKDhNfG8thgYN8M2nzVT3nQUWLlMFK3uOOx7FfYTR1uKIeNcOT5QK8gr1gGso_P6E_zBm_YqHbig5djQ_2R42mkE66R21Q2NdE8qX X   "; // Replace with your Dropbox access token
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