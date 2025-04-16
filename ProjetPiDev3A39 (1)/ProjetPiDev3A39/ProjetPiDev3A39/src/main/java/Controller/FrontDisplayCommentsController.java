package Controller;

import entite.Commentaire;
import entite.Profile;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.CommentaireService;

import java.util.List;

public class FrontDisplayCommentsController {

    @FXML
    private TableView<Commentaire> commentsTable;

    @FXML
    private TableColumn<Commentaire, String> userNameColumn;

    @FXML
    private TableColumn<Commentaire, String> commentColumn;

    @FXML
    private Label errorLabel;

    private CommentaireService commentaireService;
    private Profile profile;

    public void initialize(Profile profile) {
        System.out.println("Entering FrontDisplayCommentsController.initialize for profile ID: " + profile.getId());
        this.profile = profile;
        this.commentaireService = new CommentaireService();

        try {
            // Configure table columns
            if (userNameColumn == null) {
                throw new IllegalStateException("userNameColumn is null - check FXML file for fx:id='userNameColumn'");
            }
            userNameColumn.setCellValueFactory(cellData -> {
                String userName = cellData.getValue().getUserId() != null
                        ? (cellData.getValue().getUserId().getNom() + " " + cellData.getValue().getUserId().getPrenom()).trim()
                        : "Unknown";
                return new SimpleStringProperty(userName);
            });

            commentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getComment()));

            // Load comments
            refreshTable();
            System.out.println("FrontDisplayCommentsController initialized successfully");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing comments: " + e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<Commentaire> commentaires = commentaireService.readByProfileId(profile.getId());
            commentsTable.getItems().setAll(commentaires);
            if (commentaires.isEmpty()) {
                errorLabel.setText("No comments found for this profile.");
            } else {
                errorLabel.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading comments: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
