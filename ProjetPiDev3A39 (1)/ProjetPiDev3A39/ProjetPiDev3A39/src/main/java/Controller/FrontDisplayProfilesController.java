package Controller;

import entite.Profile;
import entite.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ProfileService;

import java.io.IOException;

public class FrontDisplayProfilesController {

    @FXML
    private TableView<Profile> profilesTable;

    @FXML
    private TableColumn<Profile, String> userIdColumn;

    @FXML
    private TableColumn<Profile, String> biographyColumn;

    @FXML
    private TableColumn<Profile, String> specialtyColumn;

    @FXML
    private TableColumn<Profile, Number> consultationPriceColumn;

    @FXML
    private TableColumn<Profile, Void> resourcesColumn;

    @FXML
    private TableColumn<Profile, Void> commentColumn;

    @FXML
    private TableColumn<Profile, Void> addCommentColumn;

    @FXML
    private TableColumn<Profile, Void> bookConsultationColumn;

    @FXML
    private Label errorLabel;

    private ProfileService profileService;

    @FXML
    public void initialize() {
        System.out.println("Entering FrontDisplayProfilesController.initialize");
        try {
            profileService = new ProfileService();
            System.out.println("ProfileService initialized");

            // Configure table columns
            userIdColumn.setCellValueFactory(cellData -> {
                User user = cellData.getValue().getUserId();
                String fullName = (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : "");
                return new SimpleStringProperty(fullName.trim());
            });
            biographyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBiographie()));
            specialtyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSpecialite()));
            consultationPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrixConsultation()));

            resourcesColumn.setCellFactory(param -> new TableCell<>() {
                private final Button resourcesButton = new Button("View PDF");

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
                private final Button commentButton = new Button("View Comments");

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

            addCommentColumn.setCellFactory(param -> new TableCell<>() {
                private final Button addCommentButton = new Button("Add Comment");

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Profile profile = getTableView().getItems().get(getIndex());
                        addCommentButton.setOnAction(event -> openAddCommentWindow(profile));
                        setGraphic(addCommentButton);
                    }
                }
            });

            bookConsultationColumn.setCellFactory(param -> new TableCell<>() {
                private final Button bookButton = new Button("Book Consultation");

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Profile profile = getTableView().getItems().get(getIndex());
                        bookButton.setOnAction(event -> openBookConsultationWindow(profile));
                        setGraphic(bookButton);
                    }
                }
            });

            // Load profiles
            profilesTable.getItems().setAll(profileService.readAll());
            System.out.println("Profiles loaded: " + profilesTable.getItems().size());

            if (profilesTable.getItems().isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontDisplayComments.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: /FrontDisplayComments.fxml");
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddComment.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontAddConsultation.fxml"));
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
            profilesTable.getItems().setAll(profileService.readAll());
            if (profilesTable.getItems().isEmpty()) {
                errorLabel.setText("No profiles found in the database.");
            } else {
                errorLabel.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error refreshing table: " + e.getMessage());
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