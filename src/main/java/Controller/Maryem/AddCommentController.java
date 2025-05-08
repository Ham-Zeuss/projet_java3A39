package Controller.Maryem;

import entite.Commentaire;
import entite.Profile;
import entite.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import service.CommentaireService;

public class AddCommentController {

    @FXML
    private TextArea commentTextArea;

    @FXML
    private TextField reportReasonField;

    @FXML
    private CheckBox reportedCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button setupButton;

    @FXML
    private Label errorLabel;

    private Profile profile;
    private CommentaireService commentaireService;

    public void initialize(Profile profile) {
        this.profile = profile;
        this.commentaireService = new CommentaireService();

        Session session = Session.getInstance();
        if (!session.isActive()) {
            errorLabel.setText("No active session. Please log in.");
        }

        // Configure buttons with icons and text
        if (saveButton != null) {
            setupButton(saveButton, "https://img.icons8.com/?size=100&id=94194&format=png&color=000000", "Save", true);
            saveButton.setOnAction(e -> saveComment());
        }

        if (cancelButton != null) {
            setupButton(cancelButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Cancel", true);
            cancelButton.setOnAction(e -> cancel());
        }
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
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            button.getStyleClass().add("icon-button");
        }
    }


    @FXML
    private void saveComment() {
        try {
            Session session = Session.getInstance();
            if (!session.isActive()) {
                errorLabel.setText("No active session. Please log in.");
                return;
            }
            int userId = session.getUserId();

            String comment = commentTextArea.getText();
            if (comment == null || comment.trim().isEmpty()) {
                errorLabel.setText("Comment cannot be empty.");
                return;
            }

            int consultationId = commentaireService.findCompletedConsultationId(userId, profile.getId());
            if (consultationId == 0) {
                errorLabel.setText("You need a completed consultation to comment.");
                return;
            }

            String reportReason = reportReasonField.getText();
            boolean reported = reportedCheckBox.isSelected();

            Commentaire commentaire = new Commentaire();
            commentaire.setUserId(userId);
            commentaire.setProfileId(profile.getId());
            commentaire.setComment(comment);
            commentaire.setConsultationId(consultationId);
            commentaire.setReportReason(reportReason != null && !reportReason.trim().isEmpty() ? reportReason : null);
            commentaire.setReported(reported);

            commentaireService.create(commentaire);
            errorLabel.setText("Comment added successfully.");

            Stage stage = (Stage) commentTextArea.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error adding comment: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) commentTextArea.getScene().getWindow();
        stage.close();
    }
}