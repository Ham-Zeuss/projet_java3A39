package Controller.Maryem;

import entite.Consultation;
import entite.Profile;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import service.ConsultationService;
import service.ProfileService;
import service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class UpdateConsultationController {

    @FXML
    private ComboBox<User> userComboBox;

    @FXML
    private ComboBox<Profile> profileComboBox;

    @FXML
    private DatePicker consultationDatePicker;

    @FXML
    private TextField consultationTimeField;

    @FXML
    private CheckBox isCompletedCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    private ConsultationService consultationService;
    private UserService userService;
    private ProfileService profileService;
    private Consultation consultation;
    private DisplayConsultationsController parentController;

    public void setConsultation(Consultation consultation, DisplayConsultationsController parentController) {
        this.consultation = consultation;
        this.parentController = parentController;
        initialize();
    }

    private void initialize() {
        try {
            consultationService = new ConsultationService();
            userService = new UserService();
            profileService = new ProfileService();

            // Populate userComboBox
            userComboBox.getItems().setAll(userService.readAll());
            userComboBox.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null ? null : user.getPrenom() + " " + user.getNom());
                }
            });
            userComboBox.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user == null ? null : user.getPrenom() + " " + user.getNom();
                }
                @Override
                public User fromString(String string) {
                    return null;
                }
            });

            // Populate profileComboBox
            profileComboBox.getItems().setAll(profileService.readAll());
            profileComboBox.setCellFactory(lv -> new ListCell<Profile>() {
                @Override
                protected void updateItem(Profile profile, boolean empty) {
                    super.updateItem(profile, empty);
                    if (empty || profile == null) {
                        setText(null);
                    } else {
                        User user = profile.getUserId();
                        String name = (user.getNom() != null ? user.getNom() : "") + " " +
                                (user.getPrenom() != null ? user.getPrenom() : "");
                        setText(name.trim().isEmpty() ? "Unknown" : name.trim());
                    }
                }
            });
            profileComboBox.setConverter(new StringConverter<Profile>() {
                @Override
                public String toString(Profile profile) {
                    if (profile == null) return null;
                    User user = profile.getUserId();
                    String name = (user.getNom() != null ? user.getNom() : "") + " " +
                            (user.getPrenom() != null ? user.getPrenom() : "");
                    return name.trim().isEmpty() ? "Unknown" : name.trim();
                }
                @Override
                public Profile fromString(String string) {
                    return null;
                }
            });

            // Pre-populate fields
            userComboBox.setValue(consultation.getUserId());
            profileComboBox.setValue(consultation.getProfileId());
            consultationDatePicker.setValue(consultation.getConsultationDate().toLocalDate());
            consultationTimeField.setText(consultation.getConsultationDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            isCompletedCheckBox.setSelected(consultation.isCompleted());

            consultationTimeField.setPromptText("HH:mm (e.g., 14:30)");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing form: " + e.getMessage());
        }
    }

    @FXML
    private void saveConsultation() {
        try {
            User selectedUser = userComboBox.getValue();
            Profile selectedProfile = profileComboBox.getValue();
            LocalDate consultationDate = consultationDatePicker.getValue();
            String timeText = consultationTimeField.getText();

            if (selectedUser == null || selectedProfile == null || consultationDate == null || timeText.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }

            if (consultationDate.isBefore(LocalDate.now())) {
                errorLabel.setText("Consultation date must be in the future.");
                return;
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime consultationTime;
            try {
                consultationTime = LocalTime.parse(timeText, timeFormatter);
            } catch (DateTimeParseException e) {
                errorLabel.setText("Invalid time format. Use HH:mm (e.g., 14:30).");
                return;
            }

            LocalDateTime consultationDateTime = consultationDate.atTime(consultationTime);

            // Check for conflict, excluding the current consultation
            if (consultationService.checkForConflict(selectedProfile.getId(), consultationDateTime) &&
                    !consultationDateTime.equals(consultation.getConsultationDate())) {
                errorLabel.setText("⏰ This time slot is already booked. Please choose another time.");
                return;
            }

            consultation.setUserId(selectedUser);
            consultation.setProfileId(selectedProfile);
            consultation.setConsultationDate(consultationDateTime);
            consultation.setCompleted(isCompletedCheckBox.isSelected());

            consultationService.update(consultation);
            errorLabel.setText("✅ Consultation updated successfully.");

            parentController.refreshTable();

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error updating consultation: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}