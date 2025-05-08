package Controller.Maryem;

import entite.Consultation;
import entite.Profile;
import entite.Session;
import entite.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private ImageView createCalendarIconView() {
        Image icon = new Image("https://img.icons8.com/?size=100&id=113573&format=png&color=000000");
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(48);
        iconView.setFitHeight(48);
        iconView.setPreserveRatio(true);
        return iconView;
    }

    private ImageView createClockIconView() {
        Image icon = new Image("https://img.icons8.com/?size=100&id=113644&format=png&color=000000");
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(48);
        iconView.setFitHeight(48);
        iconView.setPreserveRatio(true);
        return iconView;
    }

    private ImageView createProfileIconView() {
        Image icon = new Image("https://img.icons8.com/?size=100&id=8v2YbO_KCK_5&format=png&color=000000");
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(48);
        iconView.setFitHeight(48);
        iconView.setPreserveRatio(true);
        return iconView;
    }

    private ImageView createDoneIconView() {
        Image icon = new Image("https://img.icons8.com/?size=100&id=94194&format=png&color=000000");
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(48);
        iconView.setFitHeight(48);
        iconView.setPreserveRatio(true);
        return iconView;
    }

    private ImageView createNotDoneIconView() {
        Image icon = new Image("https://img.icons8.com/?size=100&id=97745&format=png&color=000000");
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(48);
        iconView.setFitHeight(48);
        iconView.setPreserveRatio(true);
        return iconView;
    }

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

                // Create an HBox for the appointment entry
                HBox appointmentBox = new HBox();
                appointmentBox.getStyleClass().add("appointment-box");
                appointmentBox.setSpacing(5);
                appointmentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Add the bullet point icon (using calendar icon)
                ImageView bulletIcon = createCalendarIconView();
                VBox.setMargin(bulletIcon, new Insets(0, 5, 0, 0));

                // Create HBox for appointment details with icons
                HBox detailsBox = new HBox();
                detailsBox.setSpacing(5);
                detailsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Create Labels with explicit styling to ensure visibility
                Label dateLabel = new Label("Date: " + date + ", ");
                dateLabel.setWrapText(true);
                dateLabel.getStyleClass().add("details-label");
                dateLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black;");

                Label timeLabel = new Label("Time: " + time + ", ");
                timeLabel.setWrapText(true);
                timeLabel.getStyleClass().add("details-label");
                timeLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black;");

                Label profileLabel = new Label("Profile: " + (profileName.trim().isEmpty() ? "Unknown" : profileName.trim()) + ", ");
                profileLabel.setWrapText(true);
                profileLabel.getStyleClass().add("details-label");
                profileLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black;");

                Label completedLabel = new Label("Completed: " + (consultation.isCompleted() ? "Yes" : "No"));
                completedLabel.setWrapText(true);
                completedLabel.getStyleClass().add("details-label");
                completedLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black;");

                // Add details with specific icons
                detailsBox.getChildren().addAll(

                        dateLabel,
                        createClockIconView(),
                        timeLabel,
                        createProfileIconView(),
                        profileLabel,
                        consultation.isCompleted() ? createDoneIconView() : createNotDoneIconView(),
                        completedLabel
                );

                // Add bullet and details to the HBox
                appointmentBox.getChildren().addAll(bulletIcon, detailsBox);

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