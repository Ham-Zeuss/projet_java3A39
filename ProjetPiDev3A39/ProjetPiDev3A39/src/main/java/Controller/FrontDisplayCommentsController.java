package Controller;

import entite.Commentaire;
import entite.Profile;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import service.CommentaireService;
import service.ProfileService;

public class FrontDisplayCommentsController {

    @FXML
    private VBox commentsContainer;

    @FXML
    private Text title;

    @FXML
    private Label errorLabel;

    private Profile profile;
    private CommentaireService commentaireService;
    private ProfileService profileService;

    public void initialize(Profile profile) {
        this.profile = profile;
        commentaireService = new CommentaireService();
        profileService = new ProfileService();

        // Update the title with the profile name
        User profileUser = profile.getUserId();
        String profileName = (profileUser.getNom() != null ? profileUser.getNom() : "") + " " +
                (profileUser.getPrenom() != null ? profileUser.getPrenom() : "");
        title.setText("Comments for " + profileName.trim());

        // Load comments
        try {
            var comments = commentaireService.readByProfileId(profile.getId());
            if (comments.isEmpty()) {
                errorLabel.setText("No comments found for this profile.");
                return;
            }

            // Dynamically create a box for each comment
            for (Commentaire comment : comments) {
                // Fetch the user who wrote the comment
                User commenter = comment.getUserId();
                String commenterName = (commenter.getNom() != null ? commenter.getNom() : "") + " " +
                        (commenter.getPrenom() != null ? commenter.getPrenom() : "");
                commenterName = commenterName.trim().isEmpty() ? "Anonymous" : commenterName.trim();

                // Create the main HBox for the comment
                HBox commentBox = new HBox();
                commentBox.getStyleClass().add("comment-box");
                commentBox.setSpacing(5);
                commentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Create separate HBoxes for each part of the message
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
                Label profileLabel = new Label(profileName.trim());
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
            errorLabel.setText("Error loading comments: " + e.getMessage());
        }
    }

    public void refreshTable() {
        commentsContainer.getChildren().clear(); // Clear existing comments
        errorLabel.setText("");
        initialize(profile); // Reload comments
    }
}