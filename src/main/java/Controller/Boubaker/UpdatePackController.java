package Controller.Boubaker;

import entite.Pack;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import service.PackService;

import java.sql.SQLException;

public class UpdatePackController {

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField validityField;

    @FXML
    private TextField featuresField;

    @FXML
    private Button updateButton;

    @FXML
    private Label errorLabel;

    private final PackService packService = new PackService();

    public void loadPackData(Pack pack) {
        idField.setText(String.valueOf(pack.getId()));
        nameField.setText(pack.getName());
        priceField.setText(String.valueOf(pack.getPrice()));
        validityField.setText(String.valueOf(pack.getValidityPeriod()));
        featuresField.setText(pack.getFeatures());
    }

    @FXML
    public void initialize() {
        updateButton.setOnAction(event -> {
            try {
                // Validate inputs
                if (nameField.getText().isEmpty() || priceField.getText().isEmpty() ||
                        validityField.getText().isEmpty() || featuresField.getText().isEmpty()) {
                    errorLabel.setText("All fields must be filled");
                    return;
                }

                double price = Double.parseDouble(priceField.getText());
                int validity = Integer.parseInt(validityField.getText());

                if (price < 0 || validity <= 0) {
                    errorLabel.setText("Invalid input: Price must be non-negative, validity must be positive");
                    return;
                }

                Pack pack = new Pack(
                        Integer.parseInt(idField.getText()),
                        nameField.getText(),
                        price,
                        featuresField.getText(),
                        validity
                );

                packService.updatePack(pack);
                errorLabel.setText("Pack updated successfully");
                // Close window after successful update
                updateButton.getScene().getWindow().hide();
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid input format: Ensure price and validity are numeric");
            } catch (RuntimeException e) {
                errorLabel.setText("Error updating pack: " + e.getMessage());
            }
        });
    }
}