package Controller;

import entite.StoreItem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import service.StoreItemService;

import java.util.regex.Pattern;

public class CreateStoreItemController {
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField priceField;
    @FXML
    private TextField imageField;
    @FXML
    private TextField stockField;
    @FXML
    private Label errorLabel;

    private StoreItemService storeItemService;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"
    );

    public CreateStoreItemController() {
        storeItemService = new StoreItemService();
    }

    @FXML
    private void handleAddItem() {
        errorLabel.setText(""); // Clear previous errors

        // Validate inputs
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = priceField.getText().trim();
        String image = imageField.getText().trim();
        String stockText = stockField.getText().trim();

        // Name validation: at least 3 characters
        if (name.isEmpty() || name.length() < 3) {
            errorLabel.setText("Name must be at least 3 characters long.");
            return;
        }

        // Description validation: optional, but if provided, at least 5 characters
        if (!description.isEmpty() && description.length() < 5) {
            errorLabel.setText("Description, if provided, must be at least 5 characters.");
            return;
        }

        // Price validation: must be positive
        int price;
        try {
            price = Integer.parseInt(priceText);
            if (price <= 0) {
                errorLabel.setText("Price must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Price must be a valid number.");
            return;
        }

        // Image URL validation: optional, but if provided, must match URL pattern
        if (!image.isEmpty() && !URL_PATTERN.matcher(image).matches()) {
            errorLabel.setText("Image URL, if provided, must be valid.");
            return;
        }

        // Stock validation: must be non-negative
        int stock;
        try {
            stock = Integer.parseInt(stockText);
            if (stock < 0) {
                errorLabel.setText("Stock cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Stock must be a valid number.");
            return;
        }

        // Create and save item
        try {
            StoreItem item = new StoreItem(
                    null, // Title is null
                    name,
                    description.isEmpty() ? null : description,
                    price,
                    image.isEmpty() ? null : image,
                    stock
            );
            storeItemService.createPst(item);
            clearFields();
            errorLabel.setText("Item added successfully!");
        } catch (Exception e) {
            errorLabel.setText("Error adding item: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        descriptionArea.clear();
        priceField.clear();
        imageField.clear();
        stockField.clear();
        errorLabel.setText("");
    }
}