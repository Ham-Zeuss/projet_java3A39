// src/main/java/Controller/CreateTitleController.java
package Controller;

import entite.StoreItem;
import entite.Title;
import service.StoreItemService;
import service.TitleService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CreateTitleController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField pointsField;

    @FXML
    private TextField priceField;

    @FXML
    private Label messageLabel;

    private TitleService titleService;
    private StoreItemService storeItemService;

    @FXML
    public void initialize() {
        titleService = new TitleService();
        storeItemService = new StoreItemService();
        clearForm(); // Initialize form

        // Listener for pointsField
        pointsField.textProperty().addListener((obs, oldValue, newValue) -> {
            String trimmed = newValue.trim();
            if (!trimmed.isEmpty() && !trimmed.equals("0")) {
                if (!priceField.getText().equals("0")) {
                    priceField.setText("0");
                }
                priceField.setDisable(true);
            } else if (priceField.getText().trim().isEmpty() || priceField.getText().equals("0")) {
                priceField.setDisable(false);
            }
        });

        // Listener for priceField
        priceField.textProperty().addListener((obs, oldValue, newValue) -> {
            String trimmed = newValue.trim();
            if (!trimmed.isEmpty() && !trimmed.equals("0")) {
                if (!pointsField.getText().equals("0")) {
                    pointsField.setText("0");
                }
                pointsField.setDisable(true);
            } else if (pointsField.getText().trim().isEmpty() || pointsField.getText().equals("0")) {
                pointsField.setDisable(false);
            }
        });
    }

    @FXML
    private void createTitle() {
        try {
            // Validate name
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                setMessage("Name is required.", false);
                return;
            }

            String pointsText = pointsField.getText().trim();
            String priceText = priceField.getText().trim();

            // Default values
            int points = 0;
            int price = 0;

            // Parse points
            if (!pointsText.isEmpty()) {
                try {
                    points = Integer.parseInt(pointsText);
                    if (points < 0) {
                        setMessage("Points Required must be non-negative.", false);
                        return;
                    }
                } catch (NumberFormatException e) {
                    setMessage("Points Required must be a valid number.", false);
                    return;
                }
            }

            // Parse price
            if (!priceText.isEmpty()) {
                try {
                    price = Integer.parseInt(priceText);
                    if (price < 0) {
                        setMessage("Price must be non-negative.", false);
                        return;
                    }
                } catch (NumberFormatException e) {
                    setMessage("Price must be a valid number.", false);
                    return;
                }
            }

            // Final validation (only one should be non-zero)
            if (points > 0 && price > 0) {
                setMessage("Only one of Points Required or Price can be set.", false);
                return;
            }

            // Create and save Title
            Title title = new Title();
            title.setName(name);
            title.setpoints_required(points);
            title.setPrice(price);

            // Create the Title (this will set the ID in the Title object)
            titleService.createPst(title);

            // If price > 0 and points == 0, add to store
            if (price > 0 && points == 0) {
                StoreItem storeItem = new StoreItem(
                        title, // Pass the Title object (which now has the ID set)
                        name,
                        "A Title that Suits you!", // Default description
                        price,
                        "https://cdn.textstudio.com/output/sample/normal/8/3/3/6/title-logo-73-16338.png", // Default image
                        999 // Default stock
                );
                storeItemService.createPst(storeItem);
                setMessage("Title and store item created successfully!", true);
            } else {
                setMessage("Title created successfully!", true);
            }

            clearForm();

        } catch (Exception e) {
            setMessage("Error creating title or store item: " + e.getMessage(), false);
        }
    }

    @FXML
    private void clearForm() {
        nameField.clear();
        pointsField.clear();
        priceField.clear();
        pointsField.setDisable(false);
        priceField.setDisable(false);
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("success", "error");
    }

    private void setMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("success", "error");
        messageLabel.getStyleClass().add(isSuccess ? "success" : "error");
    }
}