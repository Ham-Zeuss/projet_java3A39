package Controller.Maryem;

import entite.Commentaire;
import service.CommentaireService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ReportCommentController {

    @FXML
    private CheckBox inappropriateCheckBox;

    @FXML
    private CheckBox spamCheckBox;

    @FXML
    private CheckBox offensiveCheckBox;

    @FXML
    private Label errorLabel;

    private Commentaire comment;
    private CommentaireService commentaireService;

    public void initialize(Commentaire comment) {
        this.comment = comment;
        this.commentaireService = new CommentaireService();
    }

    @FXML
    private void submitReport() {
        StringBuilder reasons = new StringBuilder();
        boolean selected = false;

        if (inappropriateCheckBox.isSelected()) {
            reasons.append("Inappropriate Content,");
            selected = true;
        }
        if (spamCheckBox.isSelected()) {
            reasons.append("Spam,");
            selected = true;
        }
        if (offensiveCheckBox.isSelected()) {
            reasons.append("Offensive Language,");
            selected = true;
        }

        if (!selected) {
            errorLabel.setText("Please select at least one reason.");
            return;
        }

        try {
            // Remove trailing comma
            String reportReason = reasons.toString();
            if (reportReason.endsWith(",")) {
                reportReason = reportReason.substring(0, reportReason.length() - 1);
            }

            // Update comment
            comment.setReported(true);
            comment.setReportReason(reportReason);
            commentaireService.update(comment);

            // Close the pop-up
            Stage stage = (Stage) inappropriateCheckBox.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error submitting report: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        // Close the pop-up
        Stage stage = (Stage) inappropriateCheckBox.getScene().getWindow();
        stage.close();
    }
}
