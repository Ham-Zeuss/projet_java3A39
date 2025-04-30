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

            VBox mainContent = new VBox();

            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            if (headerFxmlLoader.getLocation() == null) {
                throw new IOException("FXML file not found: /header.fxml");
            }
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

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

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/MaryemFXML/ProfileDoctorsDetails.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IOException("FXML file not found: /MaryemFXML/ProfileDoctorsDetails.fxml");
            }
            VBox bodyContent = bodyLoader.load();
            bodyContent.setPrefHeight(1200);
            bodyContent.setMaxHeight(1500);
            mainContent.getChildren().add(bodyContent);

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

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

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

            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Profile Details");
            newStage.setResizable(false);
            newStage.show();

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