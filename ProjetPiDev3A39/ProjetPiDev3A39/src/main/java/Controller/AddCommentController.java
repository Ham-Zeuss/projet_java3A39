package Controller;

import entite.Commentaire;
import entite.Profile;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.CommentaireService;
import service.UserService;

import java.util.List;

public class AddCommentController {

    @FXML
    private ComboBox<User> userComboBox;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private TextField consultationIdField;

    @FXML
    private TextField reportReasonField;

    @FXML
    private CheckBox reportedCheckBox;

    @FXML
    private Label errorLabel;

    private Profile profile;
    private CommentaireService commentaireService;
    private UserService userService;

    public void initialize(Profile profile) {
        this.profile = profile;
        this.commentaireService = new CommentaireService();
        this.userService = new UserService();

        try {
            List<User> users = userService.readAll();
            userComboBox.getItems().setAll(users);
            userComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getNom() + " " + user.getPrenom());
                    }
                }
            });
            userComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getNom() + " " + user.getPrenom());
                    }
                }
            });

            if (!users.isEmpty()) {
                userComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading users: " + e.getMessage());
        }
    }

    @FXML
    private void saveComment() {
        try {
            User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                errorLabel.setText("Please select a user.");
                return;
            }

            String comment = commentTextArea.getText();
            if (comment == null || comment.trim().isEmpty()) {
                errorLabel.setText("Comment cannot be empty.");
                return;
            }

            String consultationIdText = consultationIdField.getText();
            if (consultationIdText == null || consultationIdText.trim().isEmpty()) {
                errorLabel.setText("Consultation ID cannot be empty.");
                return;
            }
            int consultationId;
            try {
                consultationId = Integer.parseInt(consultationIdText);
            } catch (NumberFormatException e) {
                errorLabel.setText("Consultation ID must be a valid number.");
                return;
            }

            String reportReason = reportReasonField.getText();
            boolean reported = reportedCheckBox.isSelected();

            Commentaire commentaire = new Commentaire();
            commentaire.setUserId(selectedUser);
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