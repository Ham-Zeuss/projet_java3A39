package Controller.Maryem;

import entite.Profile;
import entite.User;
import entite.Commentaire;
import service.CommentaireService;
import service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

        // Get user details (nom and prenom) from the profile's user_id
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

            // Dynamically create a box for each comment
            for (Commentaire comment : comments) {
                // Fetch the user who wrote the comment
                User commenter = comment.getUserId();
                String commenterName = (commenter.getNom() != null ? commenter.getNom() : "") + " " +
                        (commenter.getPrenom() != null ? commenter.getPrenom() : "");
                commenterName = commenterName.trim().isEmpty() ? "Anonymous" : commenterName.trim();

                // Get profile name for display
                User profileUser = profile.getUserId();
                String profileName = (profileUser.getNom() != null ? profileUser.getNom() : "") + " " +
                        (profileUser.getPrenom() != null ? profileUser.getPrenom() : "");
                profileName = profileName.trim().isEmpty() ? "Anonymous" : profileName.trim();

                // Create the main HBox for the comment
                HBox commentBox = new HBox();
                commentBox.getStyleClass().add("comment-box");
                commentBox.setSpacing(5);
                commentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Part 1: Commenter name
                HBox commenterBox = new HBox();
                commenterBox.getStyleClass().add("commenter-box");
                Label commenterLabel = new Label(commenterName);
                commenterLabel.setWrapText(true);
                commenterBox.getChildren().add(commenterLabel);

                // Part 2: "a écrit dans le profil de"
                HBox actionBox = new HBox();
                actionBox.getStyleClass().add("action-box");
                Label actionLabel = new Label("a écrit dans le profil de");
                actionLabel.setWrapText(true);
                actionBox.getChildren().add(actionLabel);

                // Part 3: Profile name
                HBox profileBox = new HBox();
                profileBox.getStyleClass().add("profile-box");
                Label profileLabel = new Label(profileName);
                profileLabel.setWrapText(true);
                profileBox.getChildren().add(profileLabel);

                // Part 4: Comment content
                HBox contentBox = new HBox();
                contentBox.getStyleClass().add("content-box");
                Label contentLabel = new Label(": " + comment.getComment());
                contentLabel.setWrapText(true);
                contentBox.getChildren().add(contentLabel);

                // Add all parts to the main comment box
                commentBox.getChildren().addAll(commenterBox, actionBox, profileBox, contentBox);

                // Add the comment box to the container
                commentsContainer.getChildren().add(commentBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
            commentsErrorLabel.setText("Error loading comments: " + e.getMessage());
        }
    }

    @FXML
    private void addComment() {
        try {
            // Fetch user with ID 1
            User commenter = userService.readById(1);
            if (commenter == null) {
                commentErrorLabel.setText("User with ID 1 not found.");
                return;
            }

            String comment = commentTextArea.getText();
            if (comment == null || comment.trim().isEmpty()) {
                commentErrorLabel.setText("Comment cannot be empty.");
                return;
            }

            Commentaire commentaire = new Commentaire();
            commentaire.setUserId(commenter);
            commentaire.setProfileId(profile.getId());
            commentaire.setComment(comment);

            commentaireService.create(commentaire);
            commentErrorLabel.setText("Comment added successfully.");

            // Clear input field
            commentTextArea.clear();

            // Refresh comments
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
                // Open the PDF file (assumes ressources is a file path)
                java.awt.Desktop.getDesktop().open(new java.io.File(profile.getRessources()));
            } catch (Exception e) {
                e.printStackTrace();
                nameLabel.setText("Error opening resources: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goBack() {
        // Close the current window
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