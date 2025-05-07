package Controller.Maryem;

import entite.Profile;
import entite.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ProfileService;

import java.io.IOException;

public class DisplayProfilesController {

    @FXML
    private TableView<Profile> profilesTable;

    @FXML
    private TableColumn<Profile, Number> idColumn;

    @FXML
    private TableColumn<Profile, String> userIdColumn;

    @FXML
    private TableColumn<Profile, String> biographyColumn;

    @FXML
    private TableColumn<Profile, String> specialtyColumn;

    @FXML
    private TableColumn<Profile, Void> resourcesColumn;

    @FXML
    private TableColumn<Profile, Number> consultationPriceColumn;

    @FXML
    private TableColumn<Profile, Number> latitudeColumn;

    @FXML
    private TableColumn<Profile, Number> longitudeColumn;

    @FXML
    private TableColumn<Profile, Void> commentColumn;



    @FXML
    private TableColumn<Profile, Void> actionColumn;

    @FXML
    private Label errorLabel;

    @FXML
    private Button addProfileButton;

    private ProfileService profileService;

    @FXML
    public void initialize() {
        System.out.println("Entering DisplayProfilesController.initialize");
        try {
            profileService = new ProfileService();
            System.out.println("ProfileService initialized");

            // Configure addProfileButton
            setupButton(addProfileButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Add Profile",true);

            // Configure table columns
            idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
            userIdColumn.setCellValueFactory(cellData -> {
                User user = cellData.getValue().getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");
                return new SimpleStringProperty(fullName.trim());
            });
            biographyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBiographie()));
            specialtyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSpecialite()));
            consultationPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrixConsultation()));
            latitudeColumn.setCellValueFactory(cellData -> {
                Double latitude = cellData.getValue().getLatitude();
                return new SimpleDoubleProperty(latitude != null ? latitude : 0.0);
            });
            longitudeColumn.setCellValueFactory(cellData -> {
                Double longitude = cellData.getValue().getLongitude();
                return new SimpleDoubleProperty(longitude != null ? longitude : 0.0);
            });

            resourcesColumn.setCellFactory(param -> new TableCell<>() {
                private final Button resourcesButton = new Button();

                {
                    setupButton(resourcesButton, "https://img.icons8.com/?size=100&id=115637&format=png&color=000000", "View PDF",false);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Profile profile = getTableView().getItems().get(getIndex());
                        resourcesButton.setOnAction(event -> openPDF(profile.getRessources()));
                        setGraphic(resourcesButton);
                    }
                }
            });

            commentColumn.setCellFactory(param -> new TableCell<>() {
                private final Button commentButton = new Button();

                {
                    setupButton(commentButton, "https://img.icons8.com/?size=100&id=116714&format=png&color=000000", "View Comments",false);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Profile profile = getTableView().getItems().get(getIndex());
                        commentButton.setOnAction(event -> openCommentsWindow(profile));
                        setGraphic(commentButton);
                    }
                }
            });


            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button();
                private final Button deleteButton = new Button();

                {
                    setupButton(editButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Edit Profile",false);
                    setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete Profile",false);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Profile profile = getTableView().getItems().get(getIndex());
                        editButton.setOnAction(event -> openEditWindow(profile));
                        deleteButton.setOnAction(event -> deleteProfile(profile));
                        setGraphic(new javafx.scene.layout.HBox(5, editButton, deleteButton));
                    }
                }
            });

            // Load profiles
            refreshTable();
            System.out.println("Profiles loaded: " + profilesTable.getItems().size());

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing profiles: " + e.getMessage());
            errorLabel.setVisible(true);
        }
        System.out.println("Exiting DisplayProfilesController.initialize");
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            // Show text only if showText is true
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 120 : 60, 60); // Larger width for buttons with text
            // Apply specified style
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 120 : 60, 60);
            // Apply same style in fallback case
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
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

    private void openCommentsWindow(Profile profile) {
        try {
            System.out.println("Attempting to open comments window for profile ID: " + profile.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/DisplayComments.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /DisplayComments.fxml");
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Comments for Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            DisplayCommentsController controller = loader.getController();
            controller.initialize(profile);

            stage.showAndWait();
            controller.refreshTable();
            System.out.println("Comments window opened and refreshed successfully");
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

    private void openEditWindow(Profile profile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/UpdateProfile.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            UpdateProfileController controller = loader.getController();
            controller.setProfile(profile, this);

            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open edit window: " + e.getMessage());
        }
    }

    @FXML
    private void openAddProfileWindow() {
        try {
            System.out.println("Attempting to open add profile window");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/AddProfile.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /AddProfile.fxml");
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add New Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();
            refreshTable();
            System.out.println("Add profile window closed and table refreshed");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open add profile window: " + e.getMessage());
        }
    }

    private void deleteProfile(Profile profile) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this profile?");
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                profileService.delete(profile);
                refreshTable();
                errorLabel.setText("Profile deleted successfully.");
                errorLabel.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Could not delete profile: " + e.getMessage());
            }
        }
    }

    public void refreshTable() {
        try {
            profilesTable.getItems().setAll(profileService.readAll());
            if (profilesTable.getItems().isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setText("");
                errorLabel.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error refreshing table: " + e.getMessage());
            errorLabel.setVisible(true);
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