package Controller.Maryem;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import entite.Commentaire;
import entite.Profile;
import entite.Session;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.CommentaireService;
import service.ProfileService;
import service.UserService;

import java.io.IOException;

public class ProfileDoctorsDetailsController {

    private static final double DEFAULT_ZOOM = 15.0;

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
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label commentsErrorLabel;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private Label commentErrorLabel;

    @FXML
    private MapView mapView;

    private Profile profile;
    private CommentaireService commentaireService;
    private UserService userService;
    private ProfileService profileService;
    private CustomMarkerLayer markerLayer;
    private MapPoint markerPosition;

    public void initialize(Profile profile) {
        this.profile = profile;
        this.commentaireService = new CommentaireService();
        this.userService = new UserService();
        this.profileService = new ProfileService();
        populateProfileDetails();
        initializeMap();
        loadComments();
        configureButtonsVisibility();
    }

    private void configureButtonsVisibility() {
        Session session = Session.getInstance();
        if (session.isActive() && profile != null && profile.getUserId() != null) {
            int loggedInUserId = session.getUserId();
            int profileUserId = profile.getUserId().getId();
            boolean isOwnProfile = loggedInUserId == profileUserId;
            updateButton.setVisible(isOwnProfile);
            updateButton.setManaged(isOwnProfile);
            deleteButton.setVisible(isOwnProfile);
            deleteButton.setManaged(isOwnProfile);
        } else {
            updateButton.setVisible(false);
            updateButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
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
        latitudeLabel.setText(profile.getLatitude() != null ? String.valueOf(profile.getLatitude()) : "N/A");
        longitudeLabel.setText(profile.getLongitude() != null ? String.valueOf(profile.getLongitude()) : "N/A");
    }

    private void initializeMap() {
        if (profile == null || profile.getLatitude() == null || profile.getLongitude() == null ||
                profile.getLatitude() == 0 || profile.getLongitude() == 0) {
            latitudeLabel.setText("N/A");
            longitudeLabel.setText("N/A");
            mapView.setDisable(true);
            return;
        }

        markerPosition = new MapPoint(profile.getLatitude(), profile.getLongitude());
        mapView.setCenter(markerPosition);
        mapView.setZoom(DEFAULT_ZOOM);

        markerLayer = new CustomMarkerLayer(mapView, markerPosition);
        mapView.addLayer(markerLayer);

        latitudeLabel.setText(String.format("%.6f", markerPosition.getLatitude()));
        longitudeLabel.setText(String.format("%.6f", markerPosition.getLongitude()));

        // Prevent map dragging by consuming mouse drag events
        mapView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> event.consume());
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

                Button reportButton = new Button("Report");
                reportButton.getStyleClass().add("report-button");
                reportButton.setDisable(comment.isReported());
                reportButton.setOnAction(event -> openReportPopup(comment));

                commentBox.getChildren().addAll(commenterBox, actionBox, profileBox, contentBox, reportButton);

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

    @FXML
    private void openUpdateProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/UpdateProfile.fxml"));
            VBox root = loader.load();

            UpdateProfileController controller = loader.getController();
            controller.setProfile(profile, null); // Pass null for parentController if not needed

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Update Profile");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh profile details after update
            profile = profileService.readById(profile.getId()); // Reload profile from database
            populateProfileDetails();
            initializeMap();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open update profile pop-up: " + e.getMessage());
        }
    }

    @FXML
    private void deleteProfile() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Profile");
        confirmation.setHeaderText("Are you sure you want to delete this profile?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    profileService.delete(profile);
                    showAlert("Success", "Profile deleted successfully.");
                    goBack(); // Close the window after deletion
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete profile: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}