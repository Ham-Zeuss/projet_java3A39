package Controller.Maryem;

import entite.Consultation;
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

public class DoctorConsultationsController {

    @FXML
    private VBox consultationsList;

    @FXML
    private Text title;

    @FXML
    private Button closeButton;

    private ConsultationService consultationService;

    @FXML
    public void initialize() {
        System.out.println("Entering DoctorConsultationsController.initialize");
        consultationService = new ConsultationService();

        try {
            // Get the current doctor's ID from Session
            Session session = Session.getInstance();
            if (!session.isActive()) {
                consultationsList.getChildren().add(new Label("No active session. Please log in."));
                return;
            }
            int doctorUserId = session.getUserId();
            System.out.println("Fetching consultations for doctor ID: " + doctorUserId);

            // Load the doctor's consultations
            var consultations = consultationService.readByDoctorUserId(doctorUserId);
            if (consultations.isEmpty()) {
                consultationsList.getChildren().add(new Label("No consultations found."));
                return;
            }

            // Date and time formatters
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Dynamically create a bullet point for each consultation
            for (Consultation consultation : consultations) {
                // Fetch patient details
                User patient = consultation.getUserId();
                String patientName = patient != null ?
                        (patient.getNom() != null ? patient.getNom() : "") + " " +
                                (patient.getPrenom() != null ? patient.getPrenom() : "") : "Unknown";

                // Format date and time from LocalDateTime
                String date = consultation.getConsultationDate() != null ?
                        consultation.getConsultationDate().format(dateFormatter) : "N/A";
                String time = consultation.getConsultationDate() != null ?
                        consultation.getConsultationDate().format(timeFormatter) : "N/A";

                // Format the consultation details with emojis/icons
                String consultationText = String.format("📅 Date: %s, ⏰ Time: %s, 👤 Patient: %s, %s Completed: %s",
                        date,
                        time,
                        patientName.trim().isEmpty() ? "Unknown" : patientName.trim(),
                        consultation.isCompleted() ? "✅" : "❌",
                        consultation.isCompleted() ? "Yes" : "No");

                // Create an HBox for the consultation entry
                HBox consultationBox = new HBox();
                consultationBox.getStyleClass().add("consultation-box");
                consultationBox.setSpacing(5);
                consultationBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Add the bullet point (using a pin emoji)
                Label bulletLabel = new Label("📌");
                bulletLabel.getStyleClass().add("bullet-label");

                // Add the consultation details
                Label detailsLabel = new Label(consultationText);
                detailsLabel.setWrapText(true);
                detailsLabel.getStyleClass().add("details-label");

                // Add bullet and details to the HBox
                consultationBox.getChildren().addAll(bulletLabel, detailsLabel);

                // Add the consultation box to the list
                consultationsList.getChildren().add(consultationBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
            consultationsList.getChildren().add(new Label("Error loading consultations: " + e.getMessage()));
        }
        System.out.println("Exiting DoctorConsultationsController.initialize");
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}