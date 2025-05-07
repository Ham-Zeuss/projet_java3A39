package Controller.Maryem;

import entite.Profile;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import service.ProfileService;

public class UpdateProfileController {

    @FXML
    private TextArea biographyField;

    @FXML
    private ComboBox<String> specialtyComboBox;

    @FXML
    private TextField resourcesField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    private ProfileService profileService;
    private Profile profileToUpdate;
    private DisplayProfilesController parentController;

    public void setProfile(Profile profile, DisplayProfilesController parentController) {
        System.out.println("Entering UpdateProfileController.setProfile");
        this.profileToUpdate = profile;
        this.parentController = parentController;
        this.profileService = new ProfileService();

        try {
            // Configure buttons with icons
            setupButton(saveButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Save Profile");
            setupButton(cancelButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Cancel");

            // Initialize the ComboBox with options
            specialtyComboBox.setItems(FXCollections.observableArrayList("Psychologue", "Nutritionniste"));

            // Pre-fill fields with profile data
            biographyField.setText(profile.getBiographie() != null ? profile.getBiographie() : "");
            specialtyComboBox.setValue(profile.getSpecialite() != null ? profile.getSpecialite() : null);
            resourcesField.setText(profile.getRessources() != null ? profile.getRessources() : "");
            priceField.setText(profile.getPrixConsultation() != 0 ? String.valueOf(profile.getPrixConsultation()) : "");
            latitudeField.setText(profile.getLatitude() != null ? String.valueOf(profile.getLatitude()) : "");
            longitudeField.setText(profile.getLongitude() != null ? String.valueOf(profile.getLongitude()) : "");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing form: " + e.getMessage());
        }
        System.out.println("Exiting UpdateProfileController.setProfile");
    }

    private void setupButton(Button button, String iconUrl, String tooltipText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            button.setText("");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.getStyleClass().add("icon-button");
        }
    }

    @FXML
    private void saveProfile() {
        try {
            // Reset error message
            errorLabel.setText("");

            // Validate fields
            String selectedSpecialty = specialtyComboBox.getValue();
            if (selectedSpecialty == null) {
                errorLabel.setText("Specialty is required.");
                return;
            }

            double price;
            try {
                price = priceField.getText().isEmpty() ? 0.0 : Double.parseDouble(priceField.getText());
                if (price < 0) {
                    errorLabel.setText("Price cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid price format.");
                return;
            }

            Double latitude = null;
            if (!latitudeField.getText().isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeField.getText());
                    if (latitude < -90 || latitude > 90) {
                        errorLabel.setText("Latitude must be between -90 and 90.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid latitude format.");
                    return;
                }
            }

            Double longitude = null;
            if (!longitudeField.getText().isEmpty()) {
                try {
                    longitude = Double.parseDouble(longitudeField.getText());
                    if (longitude < -180 || longitude > 180) {
                        errorLabel.setText("Longitude must be between -180 and 180.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid longitude format.");
                    return;
                }
            }

            // Update profile
            profileToUpdate.setBiographie(biographyField.getText().isEmpty() ? null : biographyField.getText());
            profileToUpdate.setSpecialite(selectedSpecialty);
            profileToUpdate.setRessources(resourcesField.getText().isEmpty() ? null : resourcesField.getText());
            profileToUpdate.setPrixConsultation(price);
            profileToUpdate.setLatitude(latitude);
            profileToUpdate.setLongitude(longitude);

            // Call service to update
            profileService.update(profileToUpdate);

            // Refresh table in parent controller
            if (parentController != null) {
                parentController.refreshTable();
            }

            // Close window
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error updating profile: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        // Close window without saving
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}