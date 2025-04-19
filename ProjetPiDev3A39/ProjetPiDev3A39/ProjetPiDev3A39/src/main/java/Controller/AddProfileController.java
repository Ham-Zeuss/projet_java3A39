package Controller;

import entite.Profile;
import entite.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.ProfileService;
import service.UserService;

import java.io.File;
import java.util.List;

public class AddProfileController {

    @FXML
    private ComboBox<User> userComboBox;
    @FXML
    private TextArea biographyTextArea;
    @FXML
    private ComboBox<String> specialtyComboBox;
    @FXML
    private TextField resourcesTextField;
    @FXML
    private Button chooseFileButton;
    @FXML
    private TextField priceTextField;
    @FXML
    private TextField latitudeTextField;
    @FXML
    private TextField longitudeTextField;
    @FXML
    private Button addButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label feedbackLabel;

    private ProfileService profileService;
    private UserService userService;

    @FXML
    public void initialize() {
        profileService = new ProfileService();
        userService = new UserService();

        // Populate userComboBox
        List<User> users = userService.readAll();
        System.out.println("Users loaded: " + users); // Debug
        userComboBox.setItems(FXCollections.observableArrayList(users));
        userComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getNom() + " " + user.getPrenom());
            }
        });
        userComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getNom() + " " + user.getPrenom());
            }
        });

        // Populate specialtyComboBox
        specialtyComboBox.setItems(FXCollections.observableArrayList("Psychologue", "Nutritionniste"));
        specialtyComboBox.setPromptText("Select specialty");
    }

    @FXML
    private void chooseResourceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Resource File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        Stage stage = (Stage) chooseFileButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            resourcesTextField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void addProfile() {
        try {
            feedbackLabel.setText("");
            User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                feedbackLabel.setText("Please select a user.");
                return;
            }

            String specialty = specialtyComboBox.getSelectionModel().getSelectedItem();
            if (specialty == null) {
                feedbackLabel.setText("Please select a specialty.");
                return;
            }

            String priceText = priceTextField.getText().trim();
            if (priceText.isEmpty()) {
                feedbackLabel.setText("Consultation price is required.");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) {
                    feedbackLabel.setText("Price cannot be negative.");
                    return;
                }
                if (price < 50 || price > 100) {
                    feedbackLabel.setText("Price must be between 50 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                feedbackLabel.setText("Invalid price format.");
                return;
            }

            String biography = biographyTextArea.getText().trim();
            if (biography.isEmpty()) {
                biography = null;
            }

            String resources = resourcesTextField.getText().trim();
            if (!resources.isEmpty()) {
                if (!resources.toLowerCase().endsWith(".pdf")) {
                    feedbackLabel.setText("Resources must be a .pdf file.");
                    return;
                }
            } else {
                resources = null;
            }

            Double latitude = null;
            String latitudeText = latitudeTextField.getText().trim();
            if (!latitudeText.isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeText);
                } catch (NumberFormatException e) {
                    feedbackLabel.setText("Invalid latitude format.");
                    return;
                }
            }

            Double longitude = null;
            String longitudeText = longitudeTextField.getText().trim();
            if (!longitudeText.isEmpty()) {
                try {
                    longitude = Double.parseDouble(longitudeText);
                } catch (NumberFormatException e) {
                    feedbackLabel.setText("Invalid longitude format.");
                    return;
                }
            }

            Profile profile = new Profile(
                    selectedUser,
                    biography,
                    specialty,
                    resources,
                    price,
                    latitude,
                    longitude
            );

            System.out.println("Saving profile: " + profile); // Debug
            profileService.createPst(profile);
            System.out.println("Profile saved successfully"); // Debug

            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            feedbackLabel.setText("Error adding profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}