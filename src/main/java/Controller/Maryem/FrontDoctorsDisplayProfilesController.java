package Controller.Maryem;

import entite.Profile;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ProfileService;

import java.io.IOException;
import java.net.URL;

import javafx.geometry.Insets;


import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class FrontDoctorsDisplayProfilesController {

    @FXML
    private FlowPane profilesContainer;

    @FXML
    private Label errorLabel;

    private ProfileService profileService;

    private int userId;
    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("FrontDisplayProfilesController - User ID set: " + userId);
    }

    @FXML
    public void initialize() {
        System.out.println("Entering FrontDisplayProfilesController.initialize");
        try {
            profileService = new ProfileService();
            System.out.println("ProfileService initialized");

            var profiles = profileService.readAll();
            System.out.println("Profiles loaded: " + profiles.size());

            if (profiles.isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                return;
            }

            for (Profile profile : profiles) {
                VBox profileCard = new VBox();
                profileCard.getStyleClass().add("profile-card");
                profileCard.setSpacing(5);
                profileCard.setAlignment(Pos.CENTER);

                User user = profile.getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");

                Hyperlink nameLink = new Hyperlink(fullName.trim());
                nameLink.getStyleClass().add("title-label");
                nameLink.setOnAction(event -> openProfileDetailsWindow(profile));

                Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
                Label specialtyLabel = new Label("Specialty: " + profile.getSpecialite());
                Label priceLabel = new Label("Price: $" + profile.getPrixConsultation());

                Button resourcesButton = new Button("View PDF");
                resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));

                profileCard.getChildren().addAll(
                        nameLink, bioLabel, specialtyLabel, priceLabel,
                        resourcesButton
                );

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

    private void openProfileDetailsWindow(Profile profile) {
        try {
            System.out.println("Attempting to open profile details for profile ID: " + profile.getId());

            // Create a VBox to stack header image, body, and footer
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Add header image
            ImageView headerImageView = new ImageView();
            try {
                // Load the header image from resources
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);

                // Set image properties
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1000); // As in provided code
                headerImageView.setSmooth(true);   // Better quality when scaling
                headerImageView.setCache(true);    // Better performance

                // Add some spacing below the image if needed
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                // Fallback if image fails to load
                Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 2. Load body (ProfileDoctorsDetails.fxml)
            URL fxmlResource = getClass().getResource("/MaryemFXML/ProfileDoctorsDetails.fxml");
            if (fxmlResource == null) {
                throw new IOException("Could not find ProfileDoctorsDetails.fxml at /MaryemFXML/ProfileDoctorsDetails.fxml");
            }
            System.out.println("Loading ProfileDoctorsDetails.fxml from: " + fxmlResource.toExternalForm());

            FXMLLoader bodyLoader = new FXMLLoader(fxmlResource);
            VBox bodyContent;
            try {
                bodyContent = bodyLoader.load();
            } catch (IOException e) {
                System.err.println("Failed to load ProfileDoctorsDetails.fxml: " + e.getMessage());
                throw e; // Re-throw to be caught by the outer catch block
            }
            bodyContent.setStyle("-fx-pref-width: 1000; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // 3. Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1920);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap the VBox in a ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable vertical scrollbar

            // Calculate required height
            double totalHeight = headerImageView.getFitHeight() +
                    bodyContent.prefHeight(-1) +
                    footerImageView.getFitHeight();

            // Set scene to specified window size
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }

            URL NavBar = getClass().getResource("/navbar.css");
            if (NavBar != null) {
                scene.getStylesheets().add(NavBar.toExternalForm());
            }

            // Create and configure the new stage
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Profile Details");
            newStage.setResizable(false);
            newStage.show();

            // Initialize the ProfileDoctorsDetailsController
            ProfileDoctorsDetailsController controller = bodyLoader.getController();
            controller.initialize(profile);

            System.out.println("Profile details page loaded in new window with headers and footer");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open profile details page: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error opening profile details page: " + e.getMessage());
        }
    }
    @FXML
    public void showConsultationsPage() {
        try {
            System.out.println("Navigating to consultations page");

            // Create a VBox to stack header image, body, and footer
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Add header image
            ImageView headerImageView = new ImageView();
            try {
                // Load the header image from resources
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);

                // Set image properties
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1920); // Consistent with previous transformations
                headerImageView.setSmooth(true);   // Better quality when scaling
                headerImageView.setCache(true);    // Better performance

                // Add some spacing below the image if needed
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                // Fallback if image fails to load
                Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 2. Load body (DoctorConsultations.fxml)
            URL fxmlResource = getClass().getResource("/MaryemFXML/DoctorConsultations.fxml");
            if (fxmlResource == null) {
                throw new IOException("FXML file not found: /MaryemFXML/DoctorConsultations.fxml");
            }
            System.out.println("Loading DoctorConsultations.fxml from: " + fxmlResource.toExternalForm());

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            VBox bodyContent;
            try {
                bodyContent = loader.load();
            } catch (IOException e) {
                System.err.println("Failed to load DoctorConsultations.fxml: " + e.getMessage());
                throw e; // Re-throw to be caught by the outer catch block
            }
            bodyContent.setStyle("-fx-pref-width: 1920; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // 3. Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1920);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap the VBox in a ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable vertical scrollbar

            // Calculate required height
            double totalHeight = headerImageView.getFitHeight() +
                    bodyContent.prefHeight(-1) +
                    footerImageView.getFitHeight();

            // Set up the scene
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }

            URL NavBar = getClass().getResource("/navbar.css");
            if (NavBar != null) {
                scene.getStylesheets().add(NavBar.toExternalForm());
            }

            // Set the new scene
            Stage currentStage = (Stage) profilesContainer.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("My Consultations");
            currentStage.setResizable(false); // Align with previous transformations
            currentStage.show();

            System.out.println("Consultations page loaded");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load consultations page: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error loading consultations page: " + e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            profilesContainer.getChildren().clear();
            var profiles = profileService.readAll();
            if (profiles.isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                return;
            }

            for (Profile profile : profiles) {
                VBox profileCard = new VBox();
                profileCard.getStyleClass().add("profile-card");
                profileCard.setSpacing(5);
                profileCard.setAlignment(Pos.CENTER);

                User user = profile.getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");

                Hyperlink nameLink = new Hyperlink(fullName.trim());
                nameLink.getStyleClass().add("title-label");
                nameLink.setOnAction(event -> openProfileDetailsWindow(profile));

                Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
                Label specialtyLabel = new Label("Specialty: " + profile.getSpecialite());
                Label priceLabel = new Label("Price: $" + profile.getPrixConsultation());

                Button resourcesButton = new Button("View PDF");
                resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));

                profileCard.getChildren().addAll(
                        nameLink, bioLabel, specialtyLabel, priceLabel,
                        resourcesButton
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

    public void showProfilesPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/FrontDisplayProfiles.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /MaryemFXML/FrontDisplayProfiles.fxml");
            }
            Scene scene = new Scene(loader.load(), 600, 800);
            scene.getStylesheets().add(getClass().getResource("/css/affichageprofilefront.css").toExternalForm());

            Stage currentStage = (Stage) profilesContainer.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Public Profiles");

            System.out.println("Profiles page loaded");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load profiles page: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error loading profiles page: " + e.getMessage());
        }
    }
}