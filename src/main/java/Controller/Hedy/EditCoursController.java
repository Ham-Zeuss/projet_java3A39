package Controller.Hedy;

import entite.Cours;
import entite.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.CoursService;
import service.DropboxService;

import java.io.File;
import java.time.LocalDateTime;

public class EditCoursController {

    @FXML private TextField titleField;
    @FXML private TextField pdfNameField;

    private Cours coursToEdit;
    private final CoursService coursService = new CoursService();
    private Session session = Session.getInstance();
    private String userRole = session.getRole();

    // Initialize the controller with the course to edit
    public void setCoursToEdit(Cours cours) {
        this.coursToEdit = cours;
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
            coursToEdit.setPdfName(pdfName);
            coursToEdit.setUpdatedAt(LocalDateTime.now());

            // Save the updated course to the database
            coursService.update(coursToEdit);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été modifié avec succès!");

            // Close the popup
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de la modification du cours: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        // Close the popup without redirecting
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
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
                String dropboxAccessToken ="sl.u.AFsCg3h1A_xB53v_iDv_jtAmVLHuTgvjDOGfuidJGpVVgQwi4vTp9NuWBVGCvjUUaPqes8F6SXdMYZHlW8fhLQ6OsaokAmm8q5Dh-53bGr45Wuqc9YuMXp3TsPlNke0EKln1EPOErrPnJCkQ6H9YPYfMyF5TrZIepcBpke-FjBfHa9OfJQA2iW_LARTeRnfRKSFtL4ycCQajNyv61gyT3y2pynDoSjvb-_eNao1I-eMJNhR3WW-Lcqs5hTxhZBOetFzFXexGMumJleJ_5t6jG_StIns3JW-3jRQW9uV9Fn1qRP1-dEDY669ljjQHseesIUT0Zmmr1LTzUvHZAp10Mnm9gEd8EZelloNXEIwYQ9FDso1NMw7uBFPTLwMWm7RN6kKkPGfMeaGbcemDZUgsTkfD4Hb8eDstH1eFsKzC1Ar6dQs95RYavNrDPoNPskaTo0lhQ3Yy9XD9GGpqI7Sqf9enlGNEKZsm-hl4D2X8a4LivoeS-Z-5YJv28XYy1hB5Biqh1PAxo7RM8NdyTJoiwRjv1nGopMzH10ICmIIOOC5CCYf12mRbjEmOzvNoGUwpbBkTmLzfRdpA9d3H4IZaU5zVgdXbwItmV4afOOV65e1Llrda5I-SpncZUXJ3zBAu3KWqm8YuhHvM-f7tpkRObkSkr9hkBYt4JkLbK0t-tsTFpuFM4WGc23OOMcJJ7854zFAVL8AN5WQYlMAFtvwK7tfSDZ4xtfow7jDBIVbtKOM2mvcl0o4DbSI-qpNgT6GLb-Wi6D3NPyJoGUasK59r4ETgsljHgRJO2qjZ3qi0ls6yaah3z4CFLTDijlB883iZC7nKECpGbDL6VOnlr6NpV8CZ9qN3pl02LQyjdg9bIQyjTid6To0zW4xWrCOiC8m9LZReJuoGiWiU5a0hge-DkJCr7Cz8voFR_L3oLiP_NljOiXljVcJ924f1Pbc0FWz_GQ_276GrXj7tspiPWpYwIpQxnsgc67H1MLFGl_eEFIdktn2gkv_gNIZOzxOzTa8ja-IZwHy8Af5sUKzLCE_dX5A1o2KSJNkbnC2z5ny5M0fZgtzkBJCcH-WY_M6cj_Qpv_kq63saPexwoXQ3vTByB3LI8zpjPwAWFvToqlBFX-0hcP5ATtnXKjOKhpfGLDXnPhYUHCjo-Ex9EttCp4n1wO42LJVW0e7GM5glwFntW5DxWTlM8ork00w-MeLRVRWctfF1iZ8SJ_P6BYF9Ve4KLEnmwjqFuQZlKhZ5PTPVTN45gcGJ8eRpmjCXZOnu-GC0U1N2-BORaNAmLoIqykKYf_J0";
                DropboxService dropboxService = new DropboxService(dropboxAccessToken);

                // Define the destination path in Dropbox
                String dropboxPath = "/pdfs/" + selectedFile.getName();

                // Upload the selected PDF file to Dropbox
                dropboxService.uploadFile(selectedFile.getAbsolutePath(), dropboxPath);

                // Retrieve the public URL of the uploaded file
                String fileUrl = dropboxService.getFileUrl(dropboxPath);

                if (fileUrl == null) {
                    showAlert(AlertType.ERROR, "Erreur d'upload", "Impossible de récupérer l'URL du fichier.");
                    return;
                }

                // Update the pdfNameField with the public URL
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
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}