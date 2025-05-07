package Controller.Maryem;

import entite.Consultation;
import entite.Session;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import service.ConsultationService;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;

public class DoctorConsultationsController {

    @FXML
    private VBox consultationsList;

    @FXML
    private Text title;

    @FXML
    private Button closeButton;

    @FXML
    private Button backButton;

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
                consultationBox.setAlignment(Pos.CENTER_LEFT);

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

    @FXML
    private void goBack() {
        try {
            System.out.println("Attempting to navigate back to FrontDoctorsDisplayProfiles");

            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // 1. Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1920);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(1920, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 2. Load body (FrontDoctorsDisplayProfiles.fxml)
            URL fxmlResource = getClass().getResource("/MaryemFXML/FrontDoctorsDisplayProfiles.fxml");
            if (fxmlResource == null) {
                throw new IOException("Could not find FrontDoctorsDisplayProfiles.fxml at /MaryemFXML/FrontDoctorsDisplayProfiles.fxml");
            }

            System.out.println("Loading FrontDoctorsDisplayProfiles.fxml from: " + fxmlResource.toExternalForm());

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent bodyContent;
            try {
                bodyContent = loader.load();
                bodyContent.setStyle("-fx-background-color: #B8DAB8FF;");  // Set background after loading
            } catch (IOException e) {
                System.err.println("Failed to load FrontDoctorsDisplayProfiles.fxml: " + e.getMessage());
                throw e;
            }
            mainContent.getChildren().add(bodyContent);

            // 3. Load footer
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1920);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1920, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Scene
            Scene scene = new Scene(scrollPane, 1500, 700);

            // CSS
            URL storeCards = getClass().getResource("/css/profile-card.css.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }

            URL navBar = getClass().getResource("/navbar.css");
            if (navBar != null) {
                scene.getStylesheets().add(navBar.toExternalForm());
            }

            // Stage
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Doctors Profiles");
            stage.setResizable(false);
            stage.show();

            System.out.println("FrontDoctorsDisplayProfiles page loaded with headers and footer");
        } catch (IOException e) {
            e.printStackTrace();
            consultationsList.getChildren().add(new Label("Error navigating back: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            consultationsList.getChildren().add(new Label("Unexpected error navigating back: " + e.getMessage()));
        }
    }

}