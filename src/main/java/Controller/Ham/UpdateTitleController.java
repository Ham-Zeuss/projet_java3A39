package Controller.Ham;

import entite.Title;
import entite.StoreItem;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.TitleService;
import service.StoreItemService;

public class UpdateTitleController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField pointsField;

    @FXML
    private TextField priceField;

    private Title title;
    private TitleService titleService;
    private StoreItemService storeItemService; // Add StoreItemService

    public void setTitle(Title title) {
        this.title = title;
        nameField.setText(title.getName());
        pointsField.setText(String.valueOf(title.getpoints_required()));
        priceField.setText(title.getPrice() != null ? String.valueOf(title.getPrice()) : "");
    }

    @FXML
    public void initialize() {
        titleService = new TitleService();
        storeItemService = new StoreItemService(); // Initialize StoreItemService
    }

    @FXML
    public void handleSave() {
        try {
            // Update Title fields
            title.setName(nameField.getText());
            title.setpoints_required(Integer.parseInt(pointsField.getText()));
            String priceText = priceField.getText();
            Integer newPrice = priceText.isEmpty() ? null : Integer.parseInt(priceText);
            title.setPrice(newPrice);

            // Update the Title in the database
            titleService.update(title);

            // Check if there's a StoreItem associated with this Title
            StoreItem storeItem = storeItemService.findByTitleId(title.getId());
            if (storeItem != null) {
                // Update the StoreItem's price to match the Title's price
                if (newPrice != null) {
                    storeItem.setPrice(newPrice);
                    storeItemService.update(storeItem);
                } else {
                    // If the Title's price is set to null, decide what to do with the StoreItem
                    // Option 1: Delete the StoreItem (if price is required for StoreItems)
                    storeItemService.delete(storeItem);
                    // Option 2: Set a default price (if applicable)
                    // storeItem.setPrice(0); // Example: set to 0 or another default
                    // storeItemService.update(storeItem);
                }
            } else if (newPrice != null && newPrice > 0 && title.getpoints_required() == 0) {
                // If no StoreItem exists but the Title now has a price > 0 and points == 0, create a new StoreItem
                StoreItem newStoreItem = new StoreItem(
                        title,
                        title.getName(),
                        "A Title that Suits you!", // Default description
                        newPrice,
                        "https://cdn.textstudio.com/output/sample/normal/8/3/3/6/title-logo-73-16338.png", // Default image
                        999 // Default stock
                );
                storeItemService.createPst(newStoreItem);
            }

            // Close window after saving
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace(); // Optional: show error to user
        }
    }
}