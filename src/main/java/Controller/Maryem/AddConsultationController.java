package Controller.Maryem;

import entite.Profile;
import entite.User;
import entite.Consultation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.UserService;
import service.ProfileService;
import service.ConsultationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddConsultationController {

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

    private UserService userService;
    private ProfileService profileService;
    private ConsultationService consultationService;
    private DisplayConsultationsController displayConsultationsController;

    @FXML
    public void initialize() {
        System.out.println("Entering AddConsultationController.initialize (no parameters)");
        try {
            userService = new UserService();
            profileService = new ProfileService();
            consultationService = new ConsultationService();

            // Populate ComboBoxes
            userComboBox.getItems().setAll(userService.readAll());
            profileComboBox.getItems().setAll(profileService.readAll());

            // Set default values
            isCompletedCheckBox.setSelected(false);
            consultationTimeField.setPromptText("HH:mm (e.g., 14:30)");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing form: " + e.getMessage());
        }
    }

    // For front-office: Initialize with a specific Profile
    public void initialize(Profile profile) {
        initialize(); // Call default initialization
        System.out.println("Entering AddConsultationController.initialize with Profile ID: " + profile.getId());
        try {
            // Pre-select the provided profile
            profileComboBox.getItems().clear();
            profileComboBox.getItems().add(profile);
            profileComboBox.setValue(profile);
            profileComboBox.setDisable(true); // Prevent changing the profile
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error setting profile: " + e.getMessage());
        }
    }

    // For back-office: Initialize with DisplayConsultationsController (if needed)
    public void initialize(DisplayConsultationsController controller) {
        initialize(); // Call default initialization
        System.out.println("Entering AddConsultationController.initialize with DisplayConsultationsController");
        this.displayConsultationsController = controller;
    }

    @FXML
    private void saveConsultation() {
        try {
            // Validate inputs
            User selectedUser = userComboBox.getValue();
            Profile selectedProfile = profileComboBox.getValue();
            LocalDate consultationDate = consultationDatePicker.getValue();
            String timeText = consultationTimeField.getText();

            if (selectedUser == null || selectedProfile == null || consultationDate == null || timeText.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }

            // Parse time
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime consultationTime;
            try {
                consultationTime = LocalTime.parse(timeText, timeFormatter);
            } catch (DateTimeParseException e) {
                errorLabel.setText("Invalid time format. Use HH:mm (e.g., 14:30).");
                return;
            }

            // Create and save consultation
            Consultation consultation = new Consultation();
            consultation.setUserId(selectedUser);
            consultation.setProfileId(selectedProfile);
            consultation.setConsultationDate(consultationDate.atTime(consultationTime));
            consultation.setCompleted(isCompletedCheckBox.isSelected());

            consultationService.create(consultation);
            errorLabel.setText("Consultation saved successfully.");

            // Refresh back-office consultations table if DisplayConsultationsController is provided
            if (displayConsultationsController != null) {
                displayConsultationsController.refreshTable();
            }

            // Close the window
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error saving consultation: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}