package Controller;

import entite.Consultation;
import entite.Profile;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import service.UserService;
import service.ConsultationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FrontAddConsultationController {

    @FXML
    private ComboBox<User> userComboBox;

    @FXML
    private DatePicker consultationDatePicker;

    @FXML
    private TextField consultationTimeField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    private UserService userService;
    private ConsultationService consultationService;
    private Profile profile;

    public void initialize(Profile profile) {
        System.out.println("Entering FrontAddConsultationController.initialize with Profile ID: " + profile.getId());
        this.profile = profile;
        try {
            userService = new UserService();
            consultationService = new ConsultationService();

            // Populate userComboBox
            userComboBox.getItems().setAll(userService.readAll());

            // Customize the ComboBox to display only nom and prenom
            userComboBox.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getPrenom() + " " + user.getNom());
                    }
                }
            });

            // Ensure the selected item in the ComboBox also shows nom and prenom
            userComboBox.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    if (user == null) {
                        return null;
                    }
                    return user.getPrenom() + " " + user.getNom();
                }

                @Override
                public User fromString(String string) {
                    // Not needed for this use case, as the ComboBox is not editable
                    return null;
                }
            });

            // Set default values
            consultationTimeField.setPromptText("HH:mm (e.g., 14:30)");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing form: " + e.getMessage());
        }
    }

    @FXML
    private void saveConsultation() {
        try {
            // Validate inputs
            User selectedUser = userComboBox.getValue();
            LocalDate consultationDate = consultationDatePicker.getValue();
            String timeText = consultationTimeField.getText();

            if (selectedUser == null || consultationDate == null || timeText.isEmpty()) {
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
            consultation.setProfileId(profile);
            consultation.setConsultationDate(consultationDate.atTime(consultationTime));
            consultation.setCompleted(false); // Default for front-office

            consultationService.create(consultation);
            errorLabel.setText("Consultation booked successfully.");

            // Close the window
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error booking consultation: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}