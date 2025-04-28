package Controller.Maryem;

import entite.Consultation;
import entite.Profile;
import entite.Session;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import service.ConsultationService;

import java.time.format.DateTimeFormatter;

public class UserConsultationsController {

    @FXML
    private VBox appointmentsList;

    @FXML
    private Text title;

    @FXML
    private Button closeButton;

    private ConsultationService consultationService;

    @FXML
    public void initialize() {
        System.out.println("Entering UserConsultationsController.initialize");
        consultationService = new ConsultationService();

        try {
            // Get the current user's ID from Session
            Session session = Session.getInstance();
            if (!session.isActive()) {
                appointmentsList.getChildren().add(new Label("No active session. Please log in."));
                return;
            }
            int userId = session.getUserId();
            System.out.println("Fetching consultations for user ID: " + userId);

            // Load the user's consultations
            var consultations = consultationService.readByUserId(userId);
            if (consultations.isEmpty()) {
                appointmentsList.getChildren().add(new Label("No appointments found."));
                return;
            }

            // Date and time formatters
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Dynamically create a bullet point for each consultation
            for (Consultation consultation : consultations) {
                // Fetch profile and user details
                Profile profile = consultation.getProfileId();
                User profileUser = profile != null ? profile.getUserId() : null;
                String profileName = profileUser != null ?
                        (profileUser.getNom() != null ? profileUser.getNom() : "") + " " +
                                (profileUser.getPrenom() != null ? profileUser.getPrenom() : "") : "Unknown";

                // Format date and time from LocalDateTime
                String date = consultation.getConsultationDate() != null ?
                        consultation.getConsultationDate().format(dateFormatter) : "N/A";
                String time = consultation.getConsultationDate() != null ?
                        consultation.getConsultationDate().format(timeFormatter) : "N/A";

                // Format the appointment details with emojis/icons
                String appointmentText = String.format("📅 Date: %s, ⏰ Time: %s, 👤 Profile: %s, %s Completed: %s",
                        date,
                        time,
                        profileName.trim().isEmpty() ? "Unknown" : profileName.trim(),
                        consultation.isCompleted() ? "✅" : "❌",
                        consultation.isCompleted() ? "Yes" : "No");

                // Create an HBox for the appointment entry
                HBox appointmentBox = new HBox();
                appointmentBox.getStyleClass().add("appointment-box");
                appointmentBox.setSpacing(5);
                appointmentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Add the bullet point (using a pin emoji)
                Label bulletLabel = new Label("📌");
                bulletLabel.getStyleClass().add("bullet-label");

                // Add the appointment details
                Label detailsLabel = new Label(appointmentText);
                detailsLabel.setWrapText(true);
                detailsLabel.getStyleClass().add("details-label");

                // Add bullet and details to the HBox
                appointmentBox.getChildren().addAll(bulletLabel, detailsLabel);

                // Add the appointment box to the list
                appointmentsList.getChildren().add(appointmentBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
            appointmentsList.getChildren().add(new Label("Error loading appointments: " + e.getMessage()));
        }
        System.out.println("Exiting UserConsultationsController.initialize");
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}