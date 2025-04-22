package Controller.Maryem;

import entite.Commentaire;
import entite.Profile;
import entite.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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