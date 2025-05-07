package Controller.Maryem;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import entite.Profile;
import entite.User;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.ProfileService;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class AddProfileController {

    private static final double DEFAULT_ZOOM = 15.0;
    private static final double DEFAULT_LATITUDE = 36.8065; // Example: Tunis, Tunisia
    private static final double DEFAULT_LONGITUDE = 10.1815;

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
    @FXML
    private MapView mapView;
    @FXML
    private Label mapInstructionLabel;

    private ProfileService profileService;
    private User selectedUser;
    private ObjectProperty<CustomMarkerLayer> markerLayerProperty = new SimpleObjectProperty<>(null);

    public void setSelectedUser(User user) {
        this.selectedUser = user;
    }

    @FXML
    public void initialize() {
        profileService = new ProfileService();

        // Populate specialtyComboBox
        specialtyComboBox.setItems(FXCollections.observableArrayList("Psychologue", "Nutritionniste"));
        specialtyComboBox.setPromptText("Select specialty");

        // Initialize map
        initializeMap();

        // Add listeners to update map when coordinates change
        latitudeTextField.textProperty().addListener((obs, oldValue, newValue) -> updateMap());
        longitudeTextField.textProperty().addListener((obs, oldValue, newValue) -> updateMap());

        // Bind instruction label visibility to marker presence
        mapInstructionLabel.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> markerLayerProperty.get() == null, markerLayerProperty));
        mapInstructionLabel.managedProperty().bind(mapInstructionLabel.visibleProperty());
    }

    private void initializeMap() {
        mapView.setZoom(DEFAULT_ZOOM);
        mapView.setCenter(new MapPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));
        mapView.setDisable(false); // Enable map for dragging and interaction
    }

    @FXML
    private void handleMapDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Detect double-click
            // Get the map's center and dimensions
            MapPoint center = mapView.getCenter();
            double zoom = mapView.getZoom();
            double width = mapView.getWidth();
            double height = mapView.getHeight();

            // Calculate pixels per degree/radian
            double tileSize = 256;
            double pixelsPerLonDegree = tileSize * Math.pow(2, zoom) / 360;
            double pixelsPerLonRadian = tileSize * Math.pow(2, zoom) / (2 * Math.PI);

            // Get click position relative to map center
            double clickX = event.getX() - width / 2;
            double clickY = event.getY() - height / 2;

            // Convert click position to longitude
            double lonDiff = clickX / pixelsPerLonDegree;
            double longitude = center.getLongitude() + lonDiff;

            // Convert click position to latitude (using Mercator projection)
            double centerLatRad = Math.toRadians(center.getLatitude());
            double centerMercatorY = Math.log(Math.tan(Math.PI / 4 + centerLatRad / 2));
            double mercatorY = centerMercatorY - (clickY / pixelsPerLonRadian);
            double latRad = 2 * (Math.atan(Math.exp(mercatorY)) - Math.PI / 4);
            double latitude = Math.toDegrees(latRad);

            // Validate coordinates
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                feedbackLabel.setText("Selected location is out of valid range.");
                return;
            }

            // Update text fields with locale-independent formatting
            String latText = String.format(Locale.US, "%.6f", latitude);
            String lonText = String.format(Locale.US, "%.6f", longitude);
            System.out.println("Setting coordinates: lat=" + latText + ", lon=" + lonText);
            latitudeTextField.setText(latText);
            longitudeTextField.setText(lonText);

            // Update map and marker
            updateMap();
        }
    }

    private void updateMap() {
        try {
            String latText = latitudeTextField.getText().trim();
            String lonText = longitudeTextField.getText().trim();
            System.out.println("Parsing coordinates: lat=" + latText + ", lon=" + lonText);

            if (latText.isEmpty() || lonText.isEmpty()) {
                mapView.setDisable(false); // Keep map enabled for dragging
                if (markerLayerProperty.get() != null) {
                    mapView.removeLayer(markerLayerProperty.get());
                    markerLayerProperty.set(null);
                }
                feedbackLabel.setText("");
                return;
            }

            double latitude = Double.parseDouble(latText);
            double longitude = Double.parseDouble(lonText);

            // Validate coordinate ranges
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                feedbackLabel.setText("Invalid coordinates: Latitude [-90, 90], Longitude [-180, 180]");
                mapView.setDisable(false);
                if (markerLayerProperty.get() != null) {
                    mapView.removeLayer(markerLayerProperty.get());
                    markerLayerProperty.set(null);
                }
                return;
            }

            mapView.setDisable(false);
            MapPoint newPosition = new MapPoint(latitude, longitude);
            mapView.setCenter(newPosition);

            if (markerLayerProperty.get() == null) {
                CustomMarkerLayer newLayer = new CustomMarkerLayer(mapView, newPosition);
                mapView.addLayer(newLayer);
                markerLayerProperty.set(newLayer);
            } else {
                markerLayerProperty.get().updateMarker(newPosition);
            }
            feedbackLabel.setText("");
        } catch (NumberFormatException e) {
            feedbackLabel.setText("Invalid coordinate format: " + e.getMessage());
            mapView.setDisable(false);
            if (markerLayerProperty.get() != null) {
                mapView.removeLayer(markerLayerProperty.get());
                markerLayerProperty.set(null);
            }
            System.err.println("NumberFormatException: lat=" + latitudeTextField.getText() + ", lon=" + longitudeTextField.getText());
        }
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
            if (selectedUser == null) {
                feedbackLabel.setText("No user provided for the profile.");
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
                    if (latitude < -90 || latitude > 90) {
                        feedbackLabel.setText("Latitude must be between -90 and 90.");
                        return;
                    }
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
                    if (longitude < -180 || longitude > 180) {
                        feedbackLabel.setText("Longitude must be between -180 and 180.");
                        return;
                    }
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

            profileService.createPst(profile);

            // Redirect to login.fxml
            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/login.fxml")));
                Stage currentStage = (Stage) addButton.getScene().getWindow();
                Scene scene = new Scene(root);
                currentStage.setScene(scene);
                currentStage.sizeToScene();
                currentStage.setResizable(false);
                currentStage.show();
            } catch (Exception e) {
                feedbackLabel.setText("Erreur lors du chargement de la page de connexion.");
                e.printStackTrace();
            }

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