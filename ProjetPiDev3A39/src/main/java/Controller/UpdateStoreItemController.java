// src/main/java/Controller/UpdateStoreItemController.java
package Controller;

import entite.StoreItem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import service.StoreItemService;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class UpdateStoreItemController {
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
    private StoreItem item;
    private Consumer<Void> refreshCallback;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"
    );

    public UpdateStoreItemController() {
        storeItemService = new StoreItemService();
    }

    public void initData(StoreItem item, Consumer<Void> refreshCallback) {
        this.item = item;
        this.refreshCallback = refreshCallback;

        // Populate fields
        nameField.setText(item.getName());
        descriptionArea.setText(item.getDescription() != null ? item.getDescription() : "");
        priceField.setText(String.valueOf(item.getPrice()));
        imageField.setText(item.getImage() != null ? item.getImage() : "");
        stockField.setText(String.valueOf(item.getStock()));
    }

    @FXML
    private void handleUpdateItem() {
        errorLabel.setText("");

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

        // Update item
        try {
            item.setName(name);
            item.setDescription(description.isEmpty() ? null : description);
            item.setPrice(price);
            item.setImage(image.isEmpty() ? null : image);
            item.setStock(stock);

            storeItemService.update(item);
            errorLabel.setText("Item updated successfully!");

            // Refresh the table and close the window
            refreshCallback.accept(null);
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            errorLabel.setText("Error updating item: " + e.getMessage());
        }
    }
}