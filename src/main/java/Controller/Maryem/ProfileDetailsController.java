package Controller.Maryem;

import entite.Profile;
import entite.User;
import entite.Commentaire;
import entite.Session;
import service.CommentaireService;
import service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ProfileDetailsController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label biographyLabel;

    @FXML
    private Label specialtyLabel;

    @FXML
    private Button resourcesButton;

    @FXML
    private Label priceLabel;

    @FXML
    private Label latitudeLabel;

    @FXML
    private Label longitudeLabel;

    @FXML
    private Button backButton;

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label commentsErrorLabel;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private Label commentErrorLabel;

    private Profile profile;
    private CommentaireService commentaireService;
    private UserService userService;

    public void initialize(Profile profile) {
        this.profile = profile;
        this.commentaireService = new CommentaireService();
        this.userService = new UserService();
        populateProfileDetails();
        loadComments();
    }

    private void populateProfileDetails() {
        if (profile == null) {
            nameLabel.setText("No profile selected.");
            return;
        }

        User user = profile.getUserId();
        String userName = (user.getNom() != null ? user.getNom() : "") + " " +
                (user.getPrenom() != null ? user.getPrenom() : "");
        nameLabel.setText(userName.trim().isEmpty() ? "Anonymous" : userName.trim());

        biographyLabel.setText(profile.getBiographie() != null ? profile.getBiographie() : "N/A");
        specialtyLabel.setText(profile.getSpecialite() != null ? profile.getSpecialite() : "N/A");
        resourcesButton.setDisable(profile.getRessources() == null || profile.getRessources().isEmpty());
        priceLabel.setText(profile.getPrixConsultation() != 0 ? String.format("%.2f", profile.getPrixConsultation()) : "N/A");
        latitudeLabel.setText(profile.getLatitude() != 0 ? String.valueOf(profile.getLatitude()) : "N/A");
        longitudeLabel.setText(profile.getLongitude() != 0 ? String.valueOf(profile.getLongitude()) : "N/A");
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        commentsErrorLabel.setText("");

        try {
            var comments = commentaireService.readByProfileId(profile.getId());
            if (comments.isEmpty()) {
                commentsErrorLabel.setText("No comments found for this profile.");
                return;
            }

            for (Commentaire comment : comments) {
                User commenter = userService.readById(comment.getUserId());
                String commenterName = commenter != null ?
                        (commenter.getNom() != null ? commenter.getNom() : "") + " " +
                                (commenter.getPrenom() != null ? commenter.getPrenom() : "") : "Anonymous";
                commenterName = commenterName.trim().isEmpty() ? "Anonymous" : commenterName.trim();

                User profileUser = profile.getUserId();
                String profileName = (profileUser.getNom() != null ? profileUser.getNom() : "") + " " +
                        (profileUser.getPrenom() != null ? profileUser.getPrenom() : "");
                profileName = profileName.trim().isEmpty() ? "Anonymous" : profileName.trim();

                HBox commentBox = new HBox();
                commentBox.getStyleClass().add("comment-box");
                commentBox.setSpacing(5);
                commentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                HBox commenterBox = new HBox();
                commenterBox.getStyleClass().add("commenter-box");
                Label commenterLabel = new Label(commenterName);
                commenterLabel.setWrapText(true);
                commenterBox.getChildren().add(commenterLabel);

                HBox actionBox = new HBox();
                actionBox.getStyleClass().add("action-box");
                Label actionLabel = new Label("a écrit dans le profil de");
                actionLabel.setWrapText(true);
                actionBox.getChildren().add(actionLabel);

                HBox profileBox = new HBox();
                profileBox.getStyleClass().add("profile-box");
                Label profileLabel = new Label(profileName);
                profileLabel.setWrapText(true);
                profileBox.getChildren().add(profileLabel);

                HBox contentBox = new HBox();
                contentBox.getStyleClass().add("content-box");
                Label contentLabel = new Label(": " + comment.getComment());
                contentLabel.setWrapText(true);
                contentBox.getChildren().add(contentLabel);

                commentBox.getChildren().addAll(commenterBox, actionBox, profileBox, contentBox);

                commentsContainer.getChildren().add(commentBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
            commentsErrorLabel.setText("Error loading comments: " + e.getMessage());
        }
    }

    private void openReportPopup(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/ReportComment.fxml"));
            VBox root = loader.load();

            ReportCommentController controller = loader.getController();
            controller.initialize(comment);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Report Comment");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadComments();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open report pop-up: " + e.getMessage());
        }
    }

    @FXML
    private void addComment() {
        try {
            Session session = Session.getInstance();
            if (!session.isActive()) {
                commentErrorLabel.setText("No active session. Please log in.");
                return;
            }
            int userId = session.getUserId();

            String commentText = commentTextArea.getText();
            if (commentText == null || commentText.trim().isEmpty()) {
                commentErrorLabel.setText("Comment cannot be empty.");
                return;
            }

            int consultationId = commentaireService.findCompletedConsultationId(userId, profile.getId());
            if (consultationId == 0) {
                commentErrorLabel.setText("You need a completed consultation to comment.");
                return;
            }

            Commentaire commentaire = new Commentaire();
            commentaire.setUserId(userId);
            commentaire.setProfileId(profile.getId());
            commentaire.setComment(commentText);
            commentaire.setConsultationId(consultationId);
            commentaire.setReportReason(null);
            commentaire.setReported(false);

            commentaireService.create(commentaire);
            commentErrorLabel.setText("Comment added successfully.");

            commentTextArea.clear();
            loadComments();
        } catch (Exception e) {
            e.printStackTrace();
            commentErrorLabel.setText("Error adding comment: " + e.getMessage());
        }
    }

    @FXML
    private void openResources() {
        if (profile.getRessources() != null && !profile.getRessources().isEmpty()) {
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(profile.getRessources()));
            } catch (Exception e) {
                e.printStackTrace();
                nameLabel.setText("Error opening resources: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}