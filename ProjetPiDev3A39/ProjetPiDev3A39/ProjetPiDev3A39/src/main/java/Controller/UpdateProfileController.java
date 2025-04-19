package Controller;

import entite.Profile;
import entite.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
        this.profileToUpdate = profile;
        this.parentController = parentController;
        this.profileService = new ProfileService();

        // Initialiser le ComboBox avec les options
        specialtyComboBox.setItems(FXCollections.observableArrayList("Psychologue", "Nutritionniste"));

        // Pré-remplir les champs avec les données du profil
        biographyField.setText(profile.getBiographie() != null ? profile.getBiographie() : "");
        specialtyComboBox.setValue(profile.getSpecialite() != null ? profile.getSpecialite() : null);
        resourcesField.setText(profile.getRessources() != null ? profile.getRessources() : "");
        priceField.setText(profile.getPrixConsultation() != 0 ? String.valueOf(profile.getPrixConsultation()) : "");
        latitudeField.setText(profile.getLatitude() != null ? String.valueOf(profile.getLatitude()) : "");
        longitudeField.setText(profile.getLongitude() != null ? String.valueOf(profile.getLongitude()) : "");
    }

    @FXML
    private void saveProfile() {
        try {
            // Réinitialiser le message d'erreur
            errorLabel.setText("");

            // Validation des champs
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

            // Mettre à jour le profil
            profileToUpdate.setBiographie(biographyField.getText().isEmpty() ? null : biographyField.getText());
            profileToUpdate.setSpecialite(selectedSpecialty);
            profileToUpdate.setRessources(resourcesField.getText().isEmpty() ? null : resourcesField.getText());
            profileToUpdate.setPrixConsultation(price);
            profileToUpdate.setLatitude(latitude);
            profileToUpdate.setLongitude(longitude);

            // Appeler le service pour mettre à jour
            profileService.update(profileToUpdate);

            // Rafraîchir la table dans le contrôleur parent
            if (parentController != null) {
                parentController.refreshTable();
            }

            // Fermer la fenêtre
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error updating profile: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        // Fermer la fenêtre sans sauvegarder
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}