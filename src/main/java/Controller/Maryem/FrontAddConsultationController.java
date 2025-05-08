package Controller.Maryem;

import entite.Consultation;
import entite.Profile;
import entite.Session;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import service.ConsultationService;
import service.UserService;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class FrontAddConsultationController implements Initializable {

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

    private ConsultationService consultationService;
    private UserService userService;
    private Profile profile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure buttons with icons and text
        if (saveButton != null) {
            setupButton(saveButton, "https://img.icons8.com/?size=100&id=113573&format=png&color=000000", "Book", true);
            saveButton.setOnAction(e -> saveConsultation());
        }

        if (cancelButton != null) {
            setupButton(cancelButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Cancel", true);
            cancelButton.setOnAction(e -> cancel());
        }
    }


    public void initialize(Profile profile) {
        System.out.println("Entering FrontAddConsultationController.initialize with Profile ID: " + profile.getId());
        this.profile = profile;
        try {
            consultationService = new ConsultationService();
            userService = new UserService();
            consultationTimeField.setPromptText("HH:mm (e.g., 14:30)");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing form: " + e.getMessage());
        }
    }


    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(55);
            icon.setFitHeight(55);
            button.setGraphic(icon);
            // Show text only if showText is true
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60); // Larger width for buttons with text
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            button.getStyleClass().add("icon-button");
        }
    }

    public void setProfile(Profile profile) {
        System.out.println("Entering FrontAddConsultationController.setProfile with Profile ID: " + profile.getId());
        this.profile = profile;
        try {
            consultationService = new ConsultationService();
            userService = new UserService();
            consultationTimeField.setPromptText("HH:mm (e.g., 14:30)");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing form: " + e.getMessage());
        }
    }

    @FXML
    private void saveConsultation() {
        try {
            Session session = Session.getInstance();
            if (!session.isActive()) {
                errorLabel.setText("No active session. Please log in.");
                return;
            }
            int userId = session.getUserId();
            User user = userService.readById(userId);
            if (user == null) {
                errorLabel.setText("User not found.");
                return;
            }

            LocalDate consultationDate = consultationDatePicker.getValue();
            String timeText = consultationTimeField.getText();

            if (consultationDate == null || timeText.isEmpty()) {
                errorLabel.setText("Date and time are required.");
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

            if (consultationService.checkForConflict(profile.getId(), consultationDateTime)) {
                errorLabel.setText("⏰ This time slot is already booked. Please choose another time.");
                return;
            }

            Consultation consultation = new Consultation();
            consultation.setUserId(user);
            consultation.setProfileId(profile);
            consultation.setConsultationDate(consultationDateTime);
            consultation.setCompleted(false);

            consultationService.create(consultation);
            errorLabel.setText("✅ Consultation booked successfully.");

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
            errorLabel.setText("Error booking consultation: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
