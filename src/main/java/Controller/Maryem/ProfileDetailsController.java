package Controller.Maryem;

import entite.Profile;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ProfileDetailsController {

    @FXML
    private Text title;

    @FXML
    private Label nameLabel;

    @FXML
    private Label biographyLabel;

    @FXML
    private Label specialtyLabel;

    @FXML
    private Button resourcesButton;

    @FXML
    private Label priceLabel;

    @FXML
    private Label latitudeLabel;

    @FXML
    private Label longitudeLabel;

    @FXML
    private Button backButton;

    private Profile profile;

    public void initialize(Profile profile) {
        this.profile = profile;
        populateProfileDetails();
    }

    private void populateProfileDetails() {
        if (profile == null) {
            nameLabel.setText("No profile selected.");
            return;
        }

        // Get user details (nom and prenom) from the profile's user_id
        User user = profile.getUserId();
        String userName = (user.getNom() != null ? user.getNom() : "") + " " +
                (user.getPrenom() != null ? user.getPrenom() : "");
        nameLabel.setText(userName.trim().isEmpty() ? "Anonymous" : userName.trim());

        biographyLabel.setText(profile.getBiographie() != null ? profile.getBiographie() : "N/A");
        specialtyLabel.setText(profile.getSpecialite() != null ? profile.getSpecialite() : "N/A");
        resourcesButton.setDisable(profile.getRessources() == null || profile.getRessources().isEmpty());
        priceLabel.setText(profile.getPrixConsultation() != 0 ? String.format("%.2f", profile.getPrixConsultation()) : "N/A");
        latitudeLabel.setText(profile.getLatitude() != 0 ? String.valueOf(profile.getLatitude()) : "N/A");
        longitudeLabel.setText(profile.getLongitude() != 0 ? String.valueOf(profile.getLongitude()) : "N/A");
    }

    @FXML
    private void openResources() {
        if (profile.getRessources() != null && !profile.getRessources().isEmpty()) {
            try {
                // Open the PDF file (assumes ressources is a file path)
                java.awt.Desktop.getDesktop().open(new java.io.File(profile.getRessources()));
            } catch (Exception e) {
                e.printStackTrace();
                nameLabel.setText("Error opening resources: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
