package Controller.Maryem;

import entite.Consultation;
import entite.User;
import entite.Profile;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.ConsultationService;
import service.UserService;
import service.ProfileService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private Consultation consultationToUpdate;
    private DisplayConsultationsController parentController;

    public void setConsultation(Consultation consultation, DisplayConsultationsController parentController) {
        this.consultationToUpdate = consultation;
        this.parentController = parentController;
        this.consultationService = new ConsultationService();
        this.userService = new UserService();
        this.profileService = new ProfileService();

        // Populate ComboBoxes
        List<User> users = userService.readAll();
        userComboBox.setItems(FXCollections.observableArrayList(users));
        userComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getPrenom() + " " + user.getNom());
            }
        });
        userComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getPrenom() + " " + user.getNom());
            }
        });
        userComboBox.setValue(consultation.getUserId());

        List<Profile> profiles = profileService.readAll();
        profileComboBox.setItems(FXCollections.observableArrayList(profiles));
        profileComboBox.setCellFactory(lv -> new ListCell<Profile>() {
            @Override
            protected void updateItem(Profile profile, boolean empty) {
                super.updateItem(profile, empty);
                setText(empty || profile == null ? "" :
                        (profile.getUserId() != null ? profile.getUserId().getPrenom() + " " + profile.getUserId().getNom() : "Unknown") +
                                " (" + profile.getSpecialite() + ")");
            }
        });
        profileComboBox.setButtonCell(new ListCell<Profile>() {
            @Override
            protected void updateItem(Profile profile, boolean empty) {
                super.updateItem(profile, empty);
                setText(empty || profile == null ? "" :
                        (profile.getUserId() != null ? profile.getUserId().getPrenom() + " " + profile.getUserId().getNom() : "Unknown") +
                                " (" + profile.getSpecialite() + ")");
            }
        });
        profileComboBox.setValue(consultation.getProfileId());

        // Pre-fill fields
        LocalDateTime consultationDate = consultation.getConsultationDate();
        if (consultationDate != null) {
            consultationDatePicker.setValue(consultationDate.toLocalDate());
            consultationTimeField.setText(consultationDate.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            consultationDatePicker.setValue(LocalDate.now());
            consultationTimeField.setText("09:00");
        }
        isCompletedCheckBox.setSelected(consultation.isCompleted());
    }

    @FXML
    private void saveConsultation() {
        try {
            // Validation
            if (userComboBox.getValue() == null) {
                errorLabel.setText("User is required.");
                return;
            }
            if (profileComboBox.getValue() == null) {
                errorLabel.setText("Profile is required.");
                return;
            }
            if (consultationDatePicker.getValue() == null) {
                errorLabel.setText("Consultation date is required.");
                return;
            }
            LocalTime consultationTime;
            try {
                consultationTime = LocalTime.parse(consultationTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                errorLabel.setText("Invalid time format (use HH:mm).");
                return;
            }

            // Validate time is between 09:00 and 17:00
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(17, 0);
            if (consultationTime.isBefore(startTime) || consultationTime.isAfter(endTime)) {
                errorLabel.setText("Consultation time must be between 09:00 and 17:00.");
                return;
            }

            // Combine date and time
            LocalDateTime consultationDateTime = LocalDateTime.of(consultationDatePicker.getValue(), consultationTime);

            // Validate that consultation date is strictly after current date and time
            LocalDateTime now = LocalDateTime.now();
            if (!consultationDateTime.isAfter(now)) {
                errorLabel.setText("Consultation date and time must be after the current date and time.");
                return;
            }

            // Update consultation
            consultationToUpdate.setUserId(userComboBox.getValue());
            consultationToUpdate.setProfileId(profileComboBox.getValue());
            consultationToUpdate.setConsultationDate(consultationDateTime);
            consultationToUpdate.setCompleted(isCompletedCheckBox.isSelected());

            // Save to database
            consultationService.update(consultationToUpdate);

            // Refresh parent table
            parentController.refreshTable();

            // Close window
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

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