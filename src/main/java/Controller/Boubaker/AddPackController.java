package Controller.Boubaker;

import entite.Pack;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import service.PackService;

import java.sql.SQLException;

public class AddPackController {

    @FXML private TextField nameField;
    @FXML private Label nameError;
    @FXML private TextField priceField;
    @FXML private Label priceError;
    @FXML private TextField validityField;
    @FXML private Label validityError;
    @FXML private TextArea featuresField;
    @FXML private Label featuresError;
    @FXML private Button addButton;

    private final PackService packService = new PackService();

    @FXML
    public void initialize() {
        addButton.setOnAction(event -> handleSave());
    }

    @FXML
    public void handleSave() {
        clearErrors();
        boolean isValid = true;

        if (nameField.getText().trim().isEmpty()) {
            nameError.setText("Name cannot be empty.");
            isValid = false;
        }
        if (!isValidDouble(priceField.getText())) {
            priceError.setText("Invalid price format (e.g., 99.99).");
            isValid = false;
        }
        if (!isValidInteger(validityField.getText())) {
            validityError.setText("Invalid validity period (must be an integer).");
            isValid = false;
        }
        if (featuresField.getText().trim().isEmpty()) {
            featuresError.setText("Features cannot be empty.");
            isValid = false;
        }

        if (isValid) {
            try {
                double price = Double.parseDouble(priceField.getText());
                int validity = Integer.parseInt(validityField.getText());
                String name = nameField.getText().trim();
                String features = featuresField.getText().trim();

                packService.addPack(new Pack(name, price, features, validity));
                System.out.println("Pack added successfully!");
                clearFields();
                addButton.getScene().getWindow().hide();
            } catch (RuntimeException e) {
                featuresError.setText("Error adding pack: " + e.getMessage());
            }
        }
    }

    private boolean isValidDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void clearErrors() {
        nameError.setText("");
        priceError.setText("");
        validityError.setText("");
        featuresError.setText("");
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        validityField.clear();
        featuresField.clear();
    }
}