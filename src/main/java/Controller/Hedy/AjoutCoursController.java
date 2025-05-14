package Controller.Hedy;

import entite.Session;
import entite.Cours;
import entite.Module;
import service.CoursService;
import service.ModuleService;
import service.DropboxService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;

public class AjoutCoursController {

    @FXML private TextField titleField;
    @FXML private TextField pdfNameField;
    @FXML private Button selectPdfButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Module currentModule;
    private final CoursService coursService = new CoursService();
    private Integer currentUserId;
    private Session session = Session.getInstance();

    public void setCurrentModule(Module module) {
        this.currentModule = module;
    }

    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
        System.out.println("Current User ID set to: " + currentUserId);
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            setupButton(selectPdfButton, "https://img.icons8.com/?size=100&id=115637&format=png&color=000000", "Select PDF File", true);
            setupButton(saveButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Add Course", true);
            setupButton(cancelButton, "https://img.icons8.com/?size=100&id=115638&format=png&color=000000", "Cancel", true);
        });
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        }
    }

    @FXML
    private void saveCours() {
        String title = titleField.getText().trim();
        String pdfUrl = pdfNameField.getText().trim();

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
            return;
        }

        if (pdfUrl.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Aucun fichier PDF sélectionné. Veuillez choisir un fichier PDF.");
            return;
        }

        try {
            if (currentUserId == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer l'utilisateur actuel.");
                return;
            }

            Cours newCours = new Cours(title, currentModule, pdfUrl, currentUserId);
            coursService.createPst(newCours);

            ModuleService moduleService = new ModuleService();
            currentModule.setNombreCours(currentModule.getNombreCours() + 1);
            moduleService.update(currentModule);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le cours a été ajouté avec succès!");

            // Close the popup
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de l'ajout du cours: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        // Close the popup
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
                String dropboxAccessToken ="sl.u.AFsCg3h1A_xB53v_iDv_jtAmVLHuTgvjDOGfuidJGpVVgQwi4vTp9NuWBVGCvjUUaPqes8F6SXdMYZHlW8fhLQ6OsaokAmm8q5Dh-53bGr45Wuqc9YuMXp3TsPlNke0EKln1EPOErrPnJCkQ6H9YPYfMyF5TrZIepcBpke-FjBfHa9OfJQA2iW_LARTeRnfRKSFtL4ycCQajNyv61gyT3y2pynDoSjvb-_eNao1I-eMJNhR3WW-Lcqs5hTxhZBOetFzFXexGMumJleJ_5t6jG_StIns3JW-3jRQW9uV9Fn1qRP1-dEDY669ljjQHseesIUT0Zmmr1LTzUvHZAp10Mnm9gEd8EZelloNXEIwYQ9FDso1NMw7uBFPTLwMWm7RN6kKkPGfMeaGbcemDZUgsTkfD4Hb8eDstH1eFsKzC1Ar6dQs95RYavNrDPoNPskaTo0lhQ3Yy9XD9GGpqI7Sqf9enlGNEKZsm-hl4D2X8a4LivoeS-Z-5YJv28XYy1hB5Biqh1PAxo7RM8NdyTJoiwRjv1nGopMzH10ICmIIOOC5CCYf12mRbjEmOzvNoGUwpbBkTmLzfRdpA9d3H4IZaU5zVgdXbwItmV4afOOV65e1Llrda5I-SpncZUXJ3zBAu3KWqm8YuhHvM-f7tpkRObkSkr9hkBYt4JkLbK0t-tsTFpuFM4WGc23OOMcJJ7854zFAVL8AN5WQYlMAFtvwK7tfSDZ4xtfow7jDBIVbtKOM2mvcl0o4DbSI-qpNgT6GLb-Wi6D3NPyJoGUasK59r4ETgsljHgRJO2qjZ3qi0ls6yaah3z4CFLTDijlB883iZC7nKECpGbDL6VOnlr6NpV8CZ9qN3pl02LQyjdg9bIQyjTid6To0zW4xWrCOiC8m9LZReJuoGiWiU5a0hge-DkJCr7Cz8voFR_L3oLiP_NljOiXljVcJ924f1Pbc0FWz_GQ_276GrXj7tspiPWpYwIpQxnsgc67H1MLFGl_eEFIdktn2gkv_gNIZOzxOzTa8ja-IZwHy8Af5sUKzLCE_dX5A1o2KSJNkbnC2z5ny5M0fZgtzkBJCcH-WY_M6cj_Qpv_kq63saPexwoXQ3vTByB3LI8zpjPwAWFvToqlBFX-0hcP5ATtnXKjOKhpfGLDXnPhYUHCjo-Ex9EttCp4n1wO42LJVW0e7GM5glwFntW5DxWTlM8ork00w-MeLRVRWctfF1iZ8SJ_P6BYF9Ve4KLEnmwjqFuQZlKhZ5PTPVTN45gcGJ8eRpmjCXZOnu-GC0U1N2-BORaNAmLoIqykKYf_J0";
                DropboxService dropboxService = new DropboxService(dropboxAccessToken);
                String dropboxPath = "/pdfs/" + selectedFile.getName();
                dropboxService.uploadFile(selectedFile.getAbsolutePath(), dropboxPath);
                String fileUrl = dropboxService.getFileUrl(dropboxPath);

                if (fileUrl == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur d'upload", "Impossible de récupérer l'URL du fichier.");
                    return;
                }

                pdfNameField.setText(fileUrl);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le fichier PDF a été uploadé avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'upload", "Une erreur s'est produite lors de l'upload du fichier PDF: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to show the popup
    public static void showPopup(Stage parentStage, Module module, Integer userId) {
        try {
            FXMLLoader loader = new FXMLLoader(AjoutCoursController.class.getResource("/HedyFXML/AjoutCours.fxml"));
            VBox root = loader.load();

            AjoutCoursController controller = loader.getController();
            controller.setCurrentModule(module);
            controller.setCurrentUserId(userId);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setTitle("Ajouter un Cours");

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (Exception e) {
            System.err.println("Error loading AjoutCours popup: " + e.getMessage());
        }
    }
}