package Controller.Ham;

import entite.StoreItem;
import entite.User;
import entite.UserTitle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text; // Import Text if using for stars
import service.StoreItemService;
import service.UserService;
import service.UserTitleService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListStoreItemsFrontController implements Initializable {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane itemsGrid;

    @FXML
    private Label errorLabel; // Keep this for overall errors

    private StoreItemService storeItemService;
    private UserService userService;
    private UserTitleService userTitleService;
    private ObservableList<StoreItem> storeItemsList;
    private static final int BUYER_ID = 14;
    private static final int COLUMNS = 3; // Number of cards per row
    private static final String PLACEHOLDER_IMAGE_PATH = "/Images/placeholder.png"; // Define placeholder path


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storeItemService = new StoreItemService();
        userService = new UserService();
        userTitleService = new UserTitleService();
        storeItemsList = FXCollections.observableArrayList();

        // Ensure CSS is loaded (Best practice: load in Application start or Scene creation)
        // If not loaded elsewhere, uncomment the line below (adjust path if needed):
        // scrollPane.getStylesheets().add(getClass().getResource("/path/to/your/store-cards.css").toExternalForm());

        refreshGrid();
    }

    private void refreshGrid() {
        storeItemsList.clear();
        // --- Error Handling for Service Call ---
        try {
            storeItemsList.addAll(storeItemService.readAll());
        } catch (Exception e) {
            errorLabel.setText("Error loading store items: " + e.getMessage());
            // Optionally log the full stack trace
            e.printStackTrace();
            return; // Don't proceed if items can't be loaded
        }
        // --- End Error Handling ---

        itemsGrid.getChildren().clear();
        itemsGrid.setHgap(25); // Increase horizontal gap
        itemsGrid.setVgap(25); // Increase vertical gap
        // Add padding around the grid itself
        itemsGrid.setPadding(new Insets(10));


        int column = 0;
        int row = 0;

        for (StoreItem item : storeItemsList) {
            VBox card = createItemCard(item);
            itemsGrid.add(card, column, row);
            column++;
            if (column >= COLUMNS) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createItemCard(StoreItem item) {
        // --- Card Main Container ---
        VBox card = new VBox(); // No spacing here, control with padding/margins
        card.getStyleClass().add("store-card");
        // card.setPrefWidth(250); // Let grid/content define width, maybe set min/max if needed
        card.setMaxWidth(280); // Set a max width

        // --- Image ---
        ImageView imageView = new ImageView();
        imageView.setFitWidth(260); // Adjust width slightly less than card max width if needed
        imageView.setFitHeight(180); // Adjust height as desired
        imageView.setPreserveRatio(true); // Keep aspect ratio
        imageView.setSmooth(true); // Better image quality
        try {
            // Validate URL before creating Image
            String imageUrl = item.getImage();
            if (imageUrl == null || imageUrl.isEmpty()) {
                throw new IllegalArgumentException("Image URL is null or empty.");
            }
            Image image = new Image(imageUrl, true); // Load async
            imageView.setImage(image);

            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    System.err.println("Failed to load image: " + imageUrl);
                    imageView.setImage(getPlaceholderImage());
                }
            });
            // Handle potential loading exceptions more gracefully
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    // Image loaded successfully - could add placeholder removal logic here if needed
                }
            });

        } catch (Exception e) { // Catch broader exceptions during initial load attempt
            System.err.println("Error creating image object for: " + item.getImage() + " - " + e.getMessage());
            imageView.setImage(getPlaceholderImage());
        }
        // Center image within its space if it's smaller than FitWidth/Height
        VBox imageContainer = new VBox(imageView); // Wrap image
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(5, 0, 10, 0)); // Add some space below image


        // --- Content Area (Text, Stars, Price, Stock) ---
        VBox content = new VBox();
        content.getStyleClass().add("card-content-padding"); // Apply padding & spacing via CSS


        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setWrapText(true); // Allow wrapping

        Label descriptionLabel = new Label(item.getDescription());
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true); // Allow wrapping

        Label priceLabel = new Label( item.getPrice() + "   Points!" ); // Assuming price is currency like example
        // If using points: new Label(item.getPrice() + " points");
        priceLabel.getStyleClass().add("card-price");




        Label stockLabel = new Label("Stock: " + item.getStock());
        stockLabel.getStyleClass().add("card-stock");

        // Add elements to content VBox
        content.getChildren().addAll(nameLabel, descriptionLabel, priceLabel, stockLabel);

        // --- Buy Button ---
        Button buyButton = new Button("Buy Now!"); // Match example text
        buyButton.getStyleClass().add("buy-button");
        int colorIndex = item.getId() % 4; // Cycle through 4 accent colors
        buyButton.getStyleClass().add("accent-color-" + colorIndex);
        buyButton.setMaxWidth(Double.MAX_VALUE); // Make button stretch horizontally


        // --- Status Message Label (Optional but good for feedback) ---
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setManaged(false); // Don't take space unless visible
        statusLabel.setVisible(false);
        statusLabel.setMaxWidth(Double.MAX_VALUE); // Allow centering if needed
        statusLabel.setAlignment(Pos.CENTER);


        // --- Buy Logic ---
        User buyer = null;
        try {
            buyer = userService.readByIdHamza(BUYER_ID);
        } catch (Exception e) {
            System.err.println("Error fetching buyer details: " + e.getMessage());
            // Decide how to handle this - disable all buy buttons? Show error?
            errorLabel.setText("Could not verify user data.");
        }

        boolean isTitleItem = item.getTitle() != null && item.getTitle().getId() != 0;
        boolean alreadyOwned = false;
        if (isTitleItem && buyer != null) { // Check buyer isn't null
            try {
                alreadyOwned = userTitleService.userOwnsTitle(BUYER_ID, item.getTitle().getId());
            } catch (Exception e) {
                System.err.println("Error checking title ownership: " + e.getMessage());
            }
        }

        // Determine button state and status message
        if (alreadyOwned) {
            buyButton.setDisable(true);
            statusLabel.setText("Already owned");
            statusLabel.getStyleClass().add("status-message-info");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        } else if (item.getStock() <= 0) {
            buyButton.setDisable(true);
            statusLabel.setText("Out of stock");
            statusLabel.getStyleClass().add("status-message-error"); // Use error style
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        } else if (buyer == null || buyer.getScoreTotal() == null || buyer.getScoreTotal() < item.getPrice()) {
            buyButton.setDisable(true);
            if (buyer != null) { // Only show "Low points" if buyer exists
                statusLabel.setText("Not enough points");
                statusLabel.getStyleClass().add("status-message-info"); // Info style
                statusLabel.setVisible(true);
                statusLabel.setManaged(true);
            } else {
                // Keep button disabled but don't show specific message if buyer data failed
                statusLabel.setVisible(false);
                statusLabel.setManaged(false);
            }
        } else {
            buyButton.setDisable(false);
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
        }


        // --- Buy Button Action ---
        final User finalBuyer = buyer; // Need final variable for lambda
        buyButton.setOnAction(event -> {
            // Double-check conditions before showing confirmation
            if (finalBuyer == null || finalBuyer.getScoreTotal() == null || finalBuyer.getScoreTotal() < item.getPrice() || item.getStock() <= 0) {
                errorLabel.setText("Cannot purchase item. Please check points or stock.");
                // Maybe refresh grid here in case data changed elsewhere
                refreshGrid();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Purchase");
            alert.setHeaderText("Purchase: " + item.getName());
            alert.setContentText("Cost: " + item.getPrice() + " points\nYour points: " + finalBuyer.getScoreTotal() + "\n\nProceed with purchase?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // --- Perform Purchase ---
                    int newPoints = finalBuyer.getScoreTotal() - item.getPrice();
                    userService.updatePoints(BUYER_ID, newPoints);

                    item.setStock(item.getStock() - 1);
                    storeItemService.update(item);

                    if (isTitleItem) {
                        UserTitle userTitle = new UserTitle(BUYER_ID, item.getTitle().getId());
                        userTitleService.create(userTitle);
                    }

                    // --- Success Feedback ---
                    // errorLabel.setText("Purchase successful!"); // Use general label less
                    // Optionally show a temporary success message on the card?
                    statusLabel.setText("Purchase successful!");
                    statusLabel.getStyleClass().remove("status-message-error");
                    statusLabel.getStyleClass().remove("status-message-info");
                    statusLabel.getStyleClass().add("status-message-ok");
                    statusLabel.setVisible(true);
                    statusLabel.setManaged(true);
                    // Disable button immediately after successful purchase
                    buyButton.setDisable(true);


                    // Refresh the whole grid after a short delay to show the message? Or just update this card?
                    // For simplicity, refreshing the grid ensures consistency
                    // Consider using Platform.runLater if updates are complex/delayed
                    refreshGrid(); // Refresh data & UI

                } catch (Exception e) {
                    errorLabel.setText("Purchase failed: " + e.getMessage());
                    e.printStackTrace(); // Log detailed error
                    // Optionally update the card's status label too
                    statusLabel.setText("Purchase failed!");
                    statusLabel.getStyleClass().add("status-message-error");
                    statusLabel.setVisible(true);
                    statusLabel.setManaged(true);
                }
            }
        });

        // --- Assemble Card ---
        // Add image container, content VBox, button, and status label to the main card VBox
        card.getChildren().addAll(imageContainer, content, buyButton, statusLabel);
        // Set VBox constraints if needed (e.g., make content grow)
        VBox.setVgrow(content, Priority.ALWAYS);


        return card;
    }

    // Helper method for placeholder image
    private Image getPlaceholderImage() {
        try {
            URL placeholderUrl = getClass().getResource(PLACEHOLDER_IMAGE_PATH);
            if (placeholderUrl == null) {
                System.err.println("Placeholder image not found at: " + PLACEHOLDER_IMAGE_PATH);
                // Return a null or a default constructed Image object?
                // Returning null might cause NullPointerExceptions later.
                // Creating a tiny default image might be safer if placeholder is missing.
                return null; // Or handle more robustly
            }
            return new Image(placeholderUrl.toExternalForm());
        } catch (Exception e) {
            System.err.println("Error loading placeholder image: " + e.getMessage());
            return null; // Or handle more robustly
        }
    }
}