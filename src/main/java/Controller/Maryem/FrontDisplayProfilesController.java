package Controller.Maryem;

import entite.Profile;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ProfileService;

import java.io.IOException;

public class FrontDisplayProfilesController {

    @FXML
    private FlowPane profilesContainer;

    @FXML
    private Label errorLabel;

    private ProfileService profileService;

    @FXML
    public void initialize() {
        System.out.println("Entering FrontDisplayProfilesController.initialize");
        try {
            profileService = new ProfileService();
            System.out.println("ProfileService initialized");

            // Load profiles
            var profiles = profileService.readAll();
            System.out.println("Profiles loaded: " + profiles.size());

            if (profiles.isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                return;
            }

            // Dynamically create a card for each profile
            for (Profile profile : profiles) {
                // Create a VBox for each profile card
                VBox profileCard = new VBox();
                profileCard.getStyleClass().add("profile-card");
                profileCard.setSpacing(5);
                profileCard.setAlignment(Pos.CENTER);

                // Extract user info
                User user = profile.getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");

                // Add profile details as Labels
                Label nameLabel = new Label(fullName.trim());
                nameLabel.getStyleClass().add("title-label");

                Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
                Label specialtyLabel = new Label("Specialty: " + profile.getSpecialite());
                Label priceLabel = new Label("Price: $" + profile.getPrixConsultation());

                // Create buttons with icons and tooltips
                Button resourcesButton = new Button("View PDF");
                resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));

                Button commentButton = new Button("👀");
                commentButton.getStyleClass().add("icon-button");
                commentButton.setOnAction(event -> openCommentsWindow(profile));
                Tooltip viewTooltip = new Tooltip("View Comments");
                commentButton.setTooltip(viewTooltip);

                Button addCommentButton = new Button("✍️");
                addCommentButton.getStyleClass().add("icon-button");
                addCommentButton.setOnAction(event -> openAddCommentWindow(profile));
                Tooltip addTooltip = new Tooltip("Add Comment");
                addCommentButton.setTooltip(addTooltip);

                Button bookButton = new Button("📅");
                bookButton.getStyleClass().add("icon-button");
                bookButton.setOnAction(event -> openBookConsultationWindow(profile));
                Tooltip bookTooltip = new Tooltip("Book Consultation");
                bookButton.setTooltip(bookTooltip);

                // Create an HBox to hold the icon buttons side by side
                HBox iconButtonsContainer = new HBox();
                iconButtonsContainer.setSpacing(10);
                iconButtonsContainer.setAlignment(Pos.CENTER);
                iconButtonsContainer.getChildren().addAll(commentButton, addCommentButton, bookButton);

                // Add all elements to the card
                profileCard.getChildren().addAll(
                        nameLabel, bioLabel, specialtyLabel, priceLabel,
                        resourcesButton, iconButtonsContainer
                );

                // Add the card to the container
                profilesContainer.getChildren().add(profileCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading profiles: " + e.getMessage());
        }
        System.out.println("Exiting FrontDisplayProfilesController.initialize");
    }

    private void openPDF(String pdfPath) {
        try {
            if (pdfPath != null && !pdfPath.isEmpty()) {
                System.out.println("Opening PDF: " + pdfPath);
                java.awt.Desktop.getDesktop().open(new java.io.File(pdfPath));
            } else {
                showAlert("Error", "No PDF resource available for this profile.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open PDF: " + e.getMessage());
        }
    }

    private void openCommentsWindow(Profile profile) {
        try {
            System.out.println("Attempting to open comments window for profile ID: " + profile.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/FrontDisplayComments.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /FrontDisplayComments.fxml");
            }
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load(), 500, 400); // Added size for better visibility
            scene.getStylesheets().add(getClass().getResource("/css/CommentsStyle.css").toExternalForm()); // Load CSS
            stage.setScene(scene);
            stage.setTitle("Comments for Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            FrontDisplayCommentsController controller = loader.getController();
            controller.initialize(profile);

            stage.showAndWait();
            System.out.println("Comments window opened successfully");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open comments window: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error opening comments window: " + e.getMessage());
        }
    }

    private void openAddCommentWindow(Profile profile) {
        try {
            System.out.println("Attempting to open add comment window for profile ID: " + profile.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/AddComment.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /AddComment.fxml");
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add Comment for Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            AddCommentController controller = loader.getController();
            controller.initialize(profile);

            stage.showAndWait();
            System.out.println("Add comment window closed");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open add comment window: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error opening add comment window: " + e.getMessage());
        }
    }

    private void openBookConsultationWindow(Profile profile) {
        try {
            System.out.println("Attempting to open book consultation window for profile ID: " + profile.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/FrontAddConsultation.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /FrontAddConsultation.fxml");
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Book Consultation");
            stage.initModality(Modality.APPLICATION_MODAL);

            FrontAddConsultationController controller = loader.getController();
            controller.initialize(profile);

            stage.showAndWait();
            System.out.println("Book consultation window closed");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open book consultation window: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error opening book consultation window: " + e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            profilesContainer.getChildren().clear(); // Clear existing cards
            var profiles = profileService.readAll();
            if (profiles.isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                return;
            }

            // Recreate profile cards
            for (Profile profile : profiles) {
                VBox profileCard = new VBox();
                profileCard.getStyleClass().add("profile-card");
                profileCard.setSpacing(5);
                profileCard.setAlignment(Pos.CENTER);

                User user = profile.getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");

                Label nameLabel = new Label(fullName.trim());
                nameLabel.getStyleClass().add("title-label");
                Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
                Label specialtyLabel = new Label("Specialty: " + profile.getSpecialite());
                Label priceLabel = new Label("Price: $" + profile.getPrixConsultation());

                Button resourcesButton = new Button("View PDF");
                resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));

                Button commentButton = new Button("👀");
                commentButton.getStyleClass().add("icon-button");
                commentButton.setOnAction(event -> openCommentsWindow(profile));
                Tooltip viewTooltip = new Tooltip("View Comments");
                commentButton.setTooltip(viewTooltip);

                Button addCommentButton = new Button("✍️");
                addCommentButton.getStyleClass().add("icon-button");
                addCommentButton.setOnAction(event -> openAddCommentWindow(profile));
                Tooltip addTooltip = new Tooltip("Add Comment");
                addCommentButton.setTooltip(addTooltip);

                Button bookButton = new Button("📅");
                bookButton.getStyleClass().add("icon-button");
                bookButton.setOnAction(event -> openBookConsultationWindow(profile));
                Tooltip bookTooltip = new Tooltip("Book Consultation");
                bookButton.setTooltip(bookTooltip);

                // Create an HBox to hold the icon buttons side by side
                HBox iconButtonsContainer = new HBox();
                iconButtonsContainer.setSpacing(10);
                iconButtonsContainer.setAlignment(Pos.CENTER);
                iconButtonsContainer.getChildren().addAll(commentButton, addCommentButton, bookButton);

                profileCard.getChildren().addAll(
                        nameLabel, bioLabel, specialtyLabel, priceLabel,
                        resourcesButton, iconButtonsContainer
                );

                profilesContainer.getChildren().add(profileCard);
            }
            errorLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error refreshing profiles: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}