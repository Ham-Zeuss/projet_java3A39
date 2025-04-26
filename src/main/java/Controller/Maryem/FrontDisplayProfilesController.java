package Controller.Maryem;

import entite.Profile;
import entite.User;
import entite.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Modality;
import service.ProfileService;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;

public class FrontDisplayProfilesController {

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

            // Load profiles
            var profiles = profileService.readAll();
            System.out.println("Profiles loaded: " + profiles.size());

            if (profiles.isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                return;
            }

            // Dynamically create a card for each profile
            int index = 0;
            for (Profile profile : profiles) {
                VBox profileCard = createProfileCard(profile);
                // Add staggered animation delay
                profileCard.setStyle("-fx-animation-delay: " + (index * 0.1) + "s;");
                profilesContainer.getChildren().add(profileCard);
                index++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading profiles: " + e.getMessage());
        }
        System.out.println("Exiting FrontDisplayProfilesController.initialize");
    }

    private VBox createProfileCard(Profile profile) {
        VBox profileCard = new VBox();
        profileCard.getStyleClass().add("profile-card");
        profileCard.setSpacing(5);
        profileCard.setAlignment(Pos.CENTER);

        // Extract user info
        User user = profile.getUserId();
        String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                (user.getPrenom() != null ? user.getPrenom() : "");

        // Add profile image from User entity
        ImageView profileImage;
        String photoPath = user.getPhoto();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                // Load image from the file path or URL stored in user.photo
                Image image = new Image(photoPath, true); // 'true' for background loading
                profileImage = new ImageView(image);
            } catch (Exception e) {
                System.err.println("Error loading profile image for user " + user.getId() + ": " + e.getMessage());
                // Fallback to default image
                profileImage = new ImageView(new Image(getClass().getResourceAsStream("/Images/default-profile.png")));
            }
        } else {
            // Use default image if photo is null or empty
            profileImage = new ImageView(new Image(getClass().getResourceAsStream("/Images/default-profile.png")));
        }
        profileImage.setFitWidth(80);
        profileImage.setFitHeight(80);
        Circle clip = new Circle(40, 40, 40);
        profileImage.setClip(clip);
        profileImage.setEffect(new DropShadow(5, 2, 2, Color.color(0.5, 0.5, 0.5, 0.2)));

        // Make name a clickable Hyperlink
        Hyperlink nameLink = new Hyperlink(fullName.trim());
        nameLink.setOnAction(event -> openProfileDetailsWindow(profile));

        Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
        Label specialtyLabel = new Label("Specialty: " + profile.getSpecialite());
        Label priceLabel = new Label("Price: $" + profile.getPrixConsultation());

        // Create buttons
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

        // HBox for icon buttons
        HBox iconButtonsContainer = new HBox();
        iconButtonsContainer.setSpacing(10);
        iconButtonsContainer.setAlignment(Pos.CENTER);
        iconButtonsContainer.getChildren().addAll(commentButton, addCommentButton, bookButton);

        // Add elements to card
        profileCard.getChildren().addAll(
                profileImage,
                nameLink, bioLabel, specialtyLabel, priceLabel,
                resourcesButton, iconButtonsContainer
        );

        return profileCard;
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

            // Create a VBox to stack header, header.fxml, body, and footer
            VBox mainContent = new VBox();

            // Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            if (headerFxmlLoader.getLocation() == null) {
                throw new IOException("FXML file not found: /header.fxml");
            }
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load header (header.html) using WebView
            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                System.out.println("Header URL: " + headerUrl.toExternalForm());
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                System.err.println("Error: header.html not found");
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            // Load body (ProfileDetails.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/MaryemFXML/ProfileDetails.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IOException("FXML file not found: /MaryemFXML/ProfileDetails.fxml");
            }
            VBox bodyContent = bodyLoader.load();
            bodyContent.setPrefHeight(600);
            bodyContent.setMaxHeight(600);
            mainContent.getChildren().add(bodyContent);

            // Load footer (footer.html) using WebView
            WebView footerWebView = new WebView();
            URL footerUrl = getClass().getResource("/footer.html");
            if (footerUrl != null) {
                System.out.println("Footer URL: " + footerUrl.toExternalForm());
                footerWebView.getEngine().load(footerUrl.toExternalForm());
            } else {
                System.err.println("Error: footer.html not found");
                footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
            }
            footerWebView.setPrefSize(1000, 1080);
            mainContent.getChildren().add(footerWebView);

            // Wrap the VBox in a ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Set up the scene and apply CSS
            Scene scene = new Scene(scrollPane, 1200, 700);
            URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Error: styles.css not found");
            }
            URL userTitlesCssUrl = getClass().getResource("/css/affichageprofilefront.css");
            if (userTitlesCssUrl != null) {
                scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
            } else {
                System.err.println("Error: affichageprofilefront.css not found");
            }

            // Create and configure the new stage
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Profile Details");
            newStage.setResizable(false);
            newStage.show();

            // Initialize the ProfileDetailsController
            ProfileDetailsController controller = bodyLoader.getController();
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

    private void openCommentsWindow(Profile profile) {
        try {
            System.out.println("Attempting to open comments window for profile ID: " + profile.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/FrontDisplayComments.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /FrontDisplayComments.fxml");
            }
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load(), 500, 400);
            scene.getStylesheets().add(getClass().getResource("/css/CommentsStyle.css").toExternalForm());
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
            profilesContainer.getChildren().clear();
            var profiles = profileService.readAll();
            if (profiles.isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                return;
            }

            int index = 0;
            for (Profile profile : profiles) {
                VBox profileCard = createProfileCard(profile);
                profileCard.setStyle("-fx-animation-delay: " + (index * 0.1) + "s;");
                profilesContainer.getChildren().add(profileCard);
                index++;
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