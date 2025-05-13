package Controller.Maryem;

import entite.Profile;
import entite.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import service.ProfileService;
import java.io.IOException;
import java.net.URL;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;

public class FrontDoctorsDisplayProfilesController {

    @FXML
    private FlowPane profilesContainer;

    @FXML
    private Label errorLabel;

    private ProfileService profileService;

    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("FrontDoctorsDisplayProfilesController - User ID set: " + userId);
    }

    @FXML
    public void initialize() {
        System.out.println("Entering FrontDoctorsDisplayProfilesController.initialize");
        try {
            profileService = new ProfileService();
            System.out.println("ProfileService initialized");

            // Apply CSS once the scene is ready
            Platform.runLater(() -> {
                Scene scene = profilesContainer.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(getClass().getResource("/css/profile-card.css").toExternalForm());
                    scene.getStylesheets().add(getClass().getResource("/DesignFull.css").toExternalForm());
                }
            });

            var profiles = profileService.readAll();
            System.out.println("Profiles loaded: " + profiles.size());

            if (profiles.isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                return;
            }

            for (Profile profile : profiles) {
                VBox profileCard = new VBox();
                profileCard.getStyleClass().add("pack-card");
                profileCard.setSpacing(5);
                profileCard.setAlignment(Pos.CENTER);
                profileCard.setPadding(new Insets(10));
                profileCard.setPrefWidth(220);
                profileCard.setMaxWidth(220);
                profileCard.setPrefHeight(300);

                User user = profile.getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");

                Hyperlink nameLink = new Hyperlink(fullName.trim());
                nameLink.getStyleClass().add("title-label");
                nameLink.setOnAction(event -> openProfileDetailsWindow(profile));

                Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
                bioLabel.getStyleClass().add("label");

                Label specialtyLabel = new Label("Spécialité: " + profile.getSpecialite());
                specialtyLabel.getStyleClass().add("label");

                Label priceLabel = new Label("Prix: $" + profile.getPrixConsultation());
                priceLabel.getStyleClass().add("label");

                Button resourcesButton = new Button("Voir PDF");
                resourcesButton.getStyleClass().add("button");
                try {
                    ImageView iconView = new ImageView(new Image("https://img.icons8.com/?size=100&id=115637&format=png&color=000000"));
                    iconView.setFitWidth(55);
                    iconView.setFitHeight(55);
                    iconView.setPreserveRatio(true);
                    resourcesButton.setGraphic(iconView);
                } catch (Exception e) {
                    System.err.println("Error loading resources button icon: " + e.getMessage());
                }
                resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));

                profileCard.getChildren().addAll(
                        nameLink, bioLabel, specialtyLabel, priceLabel, resourcesButton
                );

                profilesContainer.getChildren().add(profileCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading profiles: " + e.getMessage());
        }
        System.out.println("Exiting FrontDoctorsDisplayProfilesController.initialize");
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

            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            // Create a VBox to stack navbar, header image, body, and footer
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);



            // Header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(width);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load ProfileDoctorsDetails.fxml
            URL fxmlResource = getClass().getResource("/MaryemFXML/ProfileDoctorsDetails.fxml");
            if (fxmlResource == null) {
                throw new IOException("Could not find ProfileDoctorsDetails.fxml");
            }
            System.out.println("Loading ProfileDoctorsDetails.fxml from: " + fxmlResource.toExternalForm());

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            VBox bodyContent;
            try {
                bodyContent = loader.load();
            } catch (IOException e) {
                System.err.println("Failed to load ProfileDoctorsDetails.fxml: " + e.getMessage());
                throw e;
            }
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: 2000; -fx-max-height: 10000; -fx-background-color: #DCE6D7FF;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(width);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Create scene
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
            URL navBar = getClass().getResource("/css/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
            URL commentsStyle = getClass().getResource("/css/CommentsStyle.css");
            if (commentsStyle != null) scene.getStylesheets().add(commentsStyle.toExternalForm());
            URL profileCard = getClass().getResource("/css/profile-card.css");
            if (profileCard != null) scene.getStylesheets().add(profileCard.toExternalForm());
            URL designFull = getClass().getResource("/DesignFull.css");
            if (designFull != null) scene.getStylesheets().add(designFull.toExternalForm());
            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());
            URL appointments = getClass().getResource("/css/appointments.css");
            if (appointments != null) scene.getStylesheets().add(appointments.toExternalForm());
            URL gooButton = getClass().getResource("/css/GooButton.css");
            if (gooButton != null) scene.getStylesheets().add(gooButton.toExternalForm());
            URL gamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStyling != null) scene.getStylesheets().add(gamesMenuStyling.toExternalForm());
            URL oumaimaStyle = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (oumaimaStyle != null) scene.getStylesheets().add(oumaimaStyle.toExternalForm());

            // Create and configure stage
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Profile Details");
            newStage.setResizable(true);
            newStage.centerOnScreen();
            newStage.show();

            // Initialize controller
            ProfileDoctorsDetailsController controller = loader.getController();
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

            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            // Create a VBox to stack content
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);



            // Header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(width);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load DoctorConsultations.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/DoctorConsultations.fxml"));
            VBox bodyContent;
            try {
                bodyContent = loader.load();
            } catch (IOException e) {
                System.err.println("Failed to load DoctorConsultations.fxml: " + e.getMessage());
                throw e;
            }
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(width);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Create scene
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL profileCard = getClass().getResource("/css/profile-card.css");
            if (profileCard != null) scene.getStylesheets().add(profileCard.toExternalForm());
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
            URL navBar = getClass().getResource("/css/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
            URL commentsStyle = getClass().getResource("/css/CommentsStyle.css");
            if (commentsStyle != null) scene.getStylesheets().add(commentsStyle.toExternalForm());
            URL designFull = getClass().getResource("/DesignFull.css");
            if (designFull != null) scene.getStylesheets().add(designFull.toExternalForm());
            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());
            URL appointments = getClass().getResource("/css/appointments.css");
            if (appointments != null) scene.getStylesheets().add(appointments.toExternalForm());
            URL gooButton = getClass().getResource("/css/GooButton.css");
            if (gooButton != null) scene.getStylesheets().add(gooButton.toExternalForm());
            URL gamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStyling != null) scene.getStylesheets().add(gamesMenuStyling.toExternalForm());

            // Configure stage
            Stage currentStage = (Stage) profilesContainer.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("My Consultations");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.show();
            Platform.runLater(() -> scrollPane.setVvalue(0));
            System.out.println("Consultations page loaded with headers and footer");
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
                profileCard.getStyleClass().add("pack-card");
                profileCard.setSpacing(5);
                profileCard.setAlignment(Pos.CENTER);
                profileCard.setPadding(new Insets(10));
                profileCard.setPrefWidth(220);
                profileCard.setMaxWidth(220);
                profileCard.setPrefHeight(300);

                User user = profile.getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");

                Hyperlink nameLink = new Hyperlink(fullName.trim());
                nameLink.getStyleClass().add("title-label");
                nameLink.setOnAction(event -> openProfileDetailsWindow(profile));

                Label bioLabel = new Label("Bio: " + (profile.getBiographie() != null ? profile.getBiographie() : "N/A"));
                bioLabel.getStyleClass().add("label");

                Label specialtyLabel = new Label("Spécialité: " + profile.getSpecialite());
                specialtyLabel.getStyleClass().add("label");

                Label priceLabel = new Label("Prix: $" + profile.getPrixConsultation());
                priceLabel.getStyleClass().add("label");

                Button resourcesButton = new Button("Voir PDF");
                resourcesButton.getStyleClass().add("button");
                try {
                    ImageView iconView = new ImageView(new Image("https://img.icons8.com/?size=100&id=115637&format=png&color=000000"));
                    iconView.setFitWidth(55);
                    iconView.setFitHeight(55);
                    iconView.setPreserveRatio(true);
                    resourcesButton.setGraphic(iconView);
                } catch (Exception e) {
                    System.err.println("Error loading resources button icon: " + e.getMessage());
                }
                resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));

                profileCard.getChildren().addAll(
                        nameLink, bioLabel, specialtyLabel, priceLabel, resourcesButton
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
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            // Create a VBox to stack content
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);



            // Header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(width);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load FrontDisplayProfiles.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/FrontDisplayProfiles.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /MaryemFXML/FrontDisplayProfiles.fxml");
            }
            Parent bodyContent = loader.load();
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: 2000; -fx-max-height: 10000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(width);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Create scene
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL profileCard = getClass().getResource("/css/profile-card.css");
            if (profileCard != null) scene.getStylesheets().add(profileCard.toExternalForm());
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
            URL navBar = getClass().getResource("/css/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
            URL commentsStyle = getClass().getResource("/css/CommentsStyle.css");
            if (commentsStyle != null) scene.getStylesheets().add(commentsStyle.toExternalForm());
            URL designFull = getClass().getResource("/DesignFull.css");
            if (designFull != null) scene.getStylesheets().add(designFull.toExternalForm());
            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());
            URL appointments = getClass().getResource("/css/appointments.css");
            if (appointments != null) scene.getStylesheets().add(appointments.toExternalForm());
            URL gooButton = getClass().getResource("/css/GooButton.css");
            if (gooButton != null) scene.getStylesheets().add(gooButton.toExternalForm());
            URL gamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStyling != null) scene.getStylesheets().add(gamesMenuStyling.toExternalForm());
            URL oumaimaStyle = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (oumaimaStyle != null) scene.getStylesheets().add(oumaimaStyle.toExternalForm());

            // Configure stage
            Stage currentStage = (Stage) profilesContainer.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Public Profiles");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.show();

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