package Controller.Maryem;

import entite.Profile;
import entite.User;
import entite.Session;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;
import service.ProfileService;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.geometry.Insets;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import javafx.scene.shape.Rectangle;

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

            profilesContainer.setPrefWidth(1445.0); // 4 cards × 350px + 3 gaps × 15px
            profilesContainer.setMaxWidth(1445.0);

            // Dynamically create a card for each profile
            int index = 0;
            for (Profile profile : profiles) {
                StackPane profileCard = createProfileCard(profile);
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

    private StackPane createProfileCard(Profile profile) {
        // Create StackPane as the root for layering
        StackPane profileCard = new StackPane();
        profileCard.getStyleClass().add("profile-card");

        // Create a Rectangle for the gradient background (top 100px)
        Rectangle gradientBackground = new Rectangle(350, 100);
        gradientBackground.getStyleClass().add("gradient-background");
        gradientBackground.setManaged(false); // Prevent layout interference
        gradientBackground.setY(0); // Ensure it's at the top

        // Content VBox
        VBox content = new VBox();
        content.getStyleClass().add("card-content");
        content.setSpacing(5);
        content.setAlignment(Pos.CENTER);

        // Extract user info
        User user = profile.getUserId();
        String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                (user.getPrenom() != null ? user.getPrenom() : "");

        // Add profile image from User entity
        ImageView profileImage;
        String photoPath = user.getPhoto();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                // Convert the local file path to a file:// URL
                File file = new File(photoPath);
                if (file.exists()) {
                    String fileUrl = file.toURI().toURL().toString();
                    Image image = new Image(fileUrl, true);
                    profileImage = new ImageView(image);
                } else {
                    System.err.println("Profile image file does not exist: " + photoPath);
                    profileImage = new ImageView(new Image(getClass().getResourceAsStream("/Images/default-profile.png")));
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image for user " + user.getId() + ": " + e.getMessage());
                profileImage = new ImageView(new Image(getClass().getResourceAsStream("/Images/default-profile.png")));
            }
        } else {
            profileImage = new ImageView(new Image(getClass().getResourceAsStream("/Images/default-profile.png")));
        }
        profileImage.setFitWidth(80);
        profileImage.setFitHeight(80);
        Circle clip = new Circle(40, 40, 40);
        profileImage.setClip(clip);
        profileImage.setEffect(new DropShadow(5, 2, 2, Color.color(0.5, 0.5, 0.5, 0.2)));
        profileImage.getStyleClass().add("profile-image");
        VBox.setMargin(profileImage, new Insets(30, 0, 0, 0)); // Position image to overlap gradient

        // Make name a clickable Hyperlink
        Hyperlink nameLink = new Hyperlink(fullName.trim());
        nameLink.setOnAction(event -> openProfileDetailsWindow(profile));
        nameLink.getStyleClass().add("name-link");

        Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
        Label specialtyLabel = new Label("Specialty: " + profile.getSpecialite());
        Label priceLabel = new Label("Price: $" + profile.getPrixConsultation());

        // Create buttons
        Button resourcesButton = new Button();
        setupButton(resourcesButton, "https://img.icons8.com/?size=100&id=115637&format=png&color=000000", "View PDF", false);
        resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));

        Button commentButton = new Button();
        setupButton(commentButton, "https://img.icons8.com/?size=100&id=116714&format=png&color=000000", "View Comments", false);
        commentButton.setOnAction(event -> openCommentsWindow(profile));

        Button addCommentButton = new Button();
        setupButton(addCommentButton, "https://img.icons8.com/?size=100&id=117413&format=png&color=000000", "Add Comment", false);
        addCommentButton.setOnAction(event -> openAddCommentWindow(profile));

        Button bookButton = new Button();
        setupButton(bookButton, "https://img.icons8.com/?size=100&id=ia68cCcEB2RD&format=png&color=000000", "Book Consultation", false);
        bookButton.setOnAction(event -> openBookConsultationWindow(profile));

        // HBox for icon buttons
        HBox iconButtonsContainer = new HBox();
        iconButtonsContainer.getStyleClass().add("button-container");
        iconButtonsContainer.setSpacing(5);
        iconButtonsContainer.setAlignment(Pos.CENTER);
        iconButtonsContainer.getChildren().addAll(commentButton, addCommentButton, bookButton);

        // Add elements to content VBox
        content.getChildren().addAll(
                profileImage,
                nameLink, bioLabel, specialtyLabel, priceLabel,
                resourcesButton, iconButtonsContainer
        );

        // Add gradient background and content to StackPane
        profileCard.getChildren().addAll(gradientBackground, content);

        // Animation for gradient expansion on hover
        Timeline hoverAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(gradientBackground.heightProperty(), 100)),
                new KeyFrame(Duration.millis(500), new KeyValue(gradientBackground.heightProperty(), 420))
        );
        Timeline exitAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(gradientBackground.heightProperty(), 420)),
                new KeyFrame(Duration.millis(600), new KeyValue(gradientBackground.heightProperty(), 100))
        );

        // Add hover effects
        profileCard.setOnMouseEntered(event -> {
            profileCard.setEffect(new DropShadow(10, 5, 5, Color.color(0, 0, 0, 0.3)));
            hoverAnimation.play();
            profileCard.setScaleX(0.95);
            profileCard.setScaleY(0.95);
        });
        profileCard.setOnMouseExited(event -> {
            profileCard.setEffect(null);
            exitAnimation.play();
            profileCard.setScaleX(1.0);
            profileCard.setScaleY(1.0);
        });

        return profileCard;
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 100 : 60, 60);
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 100 : 60, 60);
            button.getStyleClass().add("icon-button");
        }
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

            // Create a VBox to stack header, header image, body, and footer
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1500);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(1500, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load body (ProfileDetails.fxml)
            URL fxmlResource = getClass().getResource("/MaryemFXML/ProfileDetails.fxml");
            if (fxmlResource == null) {
                throw new IOException("Could not find ProfileDetails.fxml at /MaryemFXML/ProfileDetails.fxml");
            }
            System.out.println("Loading ProfileDetails.fxml from: " + fxmlResource.toExternalForm());

            FXMLLoader bodyLoader = new FXMLLoader(fxmlResource);
            VBox bodyContent;
            try {
                bodyContent = bodyLoader.load();
            } catch (IOException e) {
                System.err.println("Failed to load ProfileDetails.fxml: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Caused by: " + e.getCause().getMessage());
                    e.getCause().printStackTrace();
                }
                throw e;
            }
            bodyContent.setStyle("-fx-pref-width: 1000; -fx-pref-height: 2000; -fx-max-height: 5000;");
            mainContent.getChildren().add(bodyContent);

            // Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1500);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1500, 100, Color.LIGHTGRAY);
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
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Get screen dimensions
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double screenWidth = bounds.getWidth();
            double screenHeight = bounds.getHeight();

            // Set scene to fit screen dimensions
            Scene scene = new Scene(scrollPane, screenWidth, screenHeight);

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
            newStage.setResizable(true);
            newStage.setMaximized(false);
            newStage.show();

            // Initialize the ProfileDetailsController
            ProfileDetailsController controller = bodyLoader.getController();
            controller.initialize(profile);

            System.out.println("Profile details page loaded in new window with headers and footer");
        } catch (IOException e) {
            e.printStackTrace();
            StringBuilder errorMessage = new StringBuilder("Could not open profile details page: ");
            errorMessage.append(e.getMessage());
            if (e.getCause() != null) {
                errorMessage.append("\nCaused by: ").append(e.getCause().getMessage());
            }
            showAlert("Error", errorMessage.toString());
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
                StackPane profileCard = createProfileCard(profile);
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
            Scene scene = new Scene(loader.load(), 1485, 800);
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
