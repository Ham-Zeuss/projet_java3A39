package Controller.Ham;

import entite.Session;
import entite.StoreItem;
import entite.User;
import entite.UserTitle;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
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
    private Label errorLabel;

    private StoreItemService storeItemService;
    private UserService userService;
    private UserTitleService userTitleService;
    private ObservableList<StoreItem> storeItemsList;
    private static final int COLUMNS = 3;
    private static final String PLACEHOLDER_IMAGE_PATH = "/Images/placeholder.png";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storeItemService = new StoreItemService();
        userService = new UserService();
        userTitleService = new UserTitleService();
        storeItemsList = FXCollections.observableArrayList();

        refreshGrid();
    }

    private void refreshGrid() {
        storeItemsList.clear();
        try {
            storeItemsList.addAll(storeItemService.readAll());
        } catch (Exception e) {
            errorLabel.setText("Error loading store items: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        itemsGrid.getChildren().clear();
        itemsGrid.setHgap(25);
        itemsGrid.setVgap(25);
        itemsGrid.setPadding(new Insets(10));

        int column = 0;
        int row = 0;

        for (StoreItem item : storeItemsList) {
            Pane card = createItemCard(item);
            itemsGrid.add(card, column, row);
            column++;
            if (column >= COLUMNS) {
                column = 0;
                row++;
            }
        }
    }

    private Pane createItemCard(StoreItem item) {
        // Main card container (VBox for vertical stacking)
        VBox card = new VBox();
        card.getStyleClass().add("card");
        // Apply random color class to card for shadow
        String[] colors = {"color-1", "color-2", "color-3", "color-4"};
        int randomIndex = (int) (Math.random() * colors.length);
        card.getStyleClass().add(colors[randomIndex]);
        card.setPrefWidth(280);
        card.setPrefHeight(400);
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(10));

        // Image container (fixed height)
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(280);
        imageContainer.setPrefHeight(140);
        imageContainer.getStyleClass().add("image-container");

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false); // Force image to fill 280x140
        imageView.setSmooth(true);

        // Clip to ensure image stays within bounds
        Rectangle clip = new Rectangle(280, 140);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        // Load image
        String imageUrl = item.getImage();
        System.out.println("Attempting to load image: " + imageUrl); // Debug log
        Image image = null;
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                System.err.println("Image URL is null or empty for item: " + item.getName());
                image = getPlaceholderImage();
            } else {
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    image = new Image(imageUrl, 280, 140, false, true, true);
                } else {
                    if (!imageUrl.startsWith("file:") && !imageUrl.startsWith("/")) {
                        imageUrl = "/" + imageUrl;
                    }
                    URL resourceUrl = getClass().getResource(imageUrl);
                    if (resourceUrl == null) {
                        System.err.println("Resource not found: " + imageUrl);
                        image = getPlaceholderImage();
                    } else {
                        image = new Image(resourceUrl.toExternalForm(), 280, 140, false, true, true);
                    }
                }
            }
            if (image != null && image.isError()) {
                System.err.println("Image load error for: " + imageUrl);
                image = getPlaceholderImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + item.getName() + ": " + e.getMessage());
            image = getPlaceholderImage();
        }

        imageView.setImage(image != null ? image : getPlaceholderImage());
        imageContainer.getChildren().add(imageView);

        // Text area
        VBox content = new VBox();
        content.setPrefWidth(280);
        content.setPrefHeight(140);
        content.setSpacing(5);
        content.setAlignment(Pos.CENTER); // Center text vertically and horizontally
        content.setPadding(new Insets(10));
        content.getStyleClass().add("card-details");

        Text nameLabel = new Text(item.getName());
        nameLabel.getStyleClass().add("text-title");
        nameLabel.setWrappingWidth(260);
        nameLabel.setTextAlignment(TextAlignment.CENTER); // Center text

        Text descriptionLabel = new Text(item.getDescription());
        descriptionLabel.getStyleClass().add("text-body");
        descriptionLabel.setWrappingWidth(260);
        descriptionLabel.setTextAlignment(TextAlignment.CENTER); // Center text

        Text priceLabel = new Text(item.getPrice() + " Points!");
        priceLabel.getStyleClass().add("text-body");
        priceLabel.setTextAlignment(TextAlignment.CENTER); // Center text

        Text stockLabel = new Text("Stock: " + item.getStock());
        stockLabel.getStyleClass().add("text-body");
        stockLabel.setTextAlignment(TextAlignment.CENTER); // Center text

        content.getChildren().addAll(nameLabel, descriptionLabel, priceLabel, stockLabel);

        // Status label (always managed to reserve space)
        Label statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setPrefWidth(280);
        statusLabel.setPrefHeight(20);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setManaged(true); // Always reserve space

        // Buy button
        Button buyButton = new Button("Buy Now!");
        buyButton.getStyleClass().add("card-button");
        buyButton.getStyleClass().add(colors[randomIndex]); // Same color as card
        buyButton.setPrefWidth(200);
        buyButton.setPrefHeight(40);
        buyButton.setAlignment(Pos.CENTER);

        // Assemble card
        card.getChildren().addAll(imageContainer, content, statusLabel, buyButton);

        // Session and purchase logic
        Session session = Session.getInstance();
        int buyerId = session.getUserId();
        User buyer = null;
        try {
            if (!session.isActive() || buyerId == 0) {
                throw new IllegalStateException("No active user session.");
            }
            buyer = userService.readByIdHamza(buyerId);
        } catch (Exception e) {
            System.err.println("Error fetching buyer details: " + e.getMessage());
            errorLabel.setText("Could not verify user data.");
        }

        boolean isTitleItem = item.getTitle() != null && item.getTitle().getId() != 0;
        boolean alreadyOwned = false;
        if (isTitleItem && buyer != null) {
            try {
                alreadyOwned = userTitleService.userOwnsTitle(buyerId, item.getTitle().getId());
            } catch (Exception e) {
                System.err.println("Error checking title ownership: " + e.getMessage());
            }
        }

        if (alreadyOwned) {
            buyButton.setDisable(true);
            statusLabel.setText("Already owned");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else if (item.getStock() <= 0) {
            buyButton.setDisable(true);
            statusLabel.setText("Out of stock");
            statusLabel.getStyleClass().add("status-message-error");
        } else if (buyer == null || buyer.getScoreTotal() == null || buyer.getScoreTotal() < item.getPrice()) {
            buyButton.setDisable(true);
            if (buyer != null) {
                statusLabel.setText("Not enough points");
                statusLabel.getStyleClass().add("status-message-info");
            } else {
                statusLabel.setText("Please log in.");
                statusLabel.getStyleClass().add("status-message-info");
            }
        } else {
            buyButton.setDisable(false);
            statusLabel.setText(""); // Empty text, space reserved
        }

        final User finalBuyer = buyer;
        buyButton.setOnAction(event -> {
            if (finalBuyer == null || finalBuyer.getScoreTotal() == null || finalBuyer.getScoreTotal() < item.getPrice() || item.getStock() <= 0) {
                errorLabel.setText("Cannot purchase item. Please check points or stock.");
                refreshGrid();
                return;
            }

            // Custom dialog
            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setTitle("Confirm Purchase");

            // Dialog content
            VBox dialogContent = new VBox();
            dialogContent.getStyleClass().add("dialog-card");
            dialogContent.setPrefWidth(320);
            dialogContent.setSpacing(16);
            dialogContent.setPadding(new Insets(16));

            // Header
            HBox header = new HBox();
            header.getStyleClass().add("dialog-header");
            header.setSpacing(16);
            header.setAlignment(Pos.CENTER_LEFT);

            // Icon
            StackPane iconPane = new StackPane();
            iconPane.getStyleClass().add("dialog-icon");
            ImageView icon = new ImageView(new Image("Images/confirmation_icon_Hamza.png", 16, 16, true, true));
            iconPane.getChildren().add(icon);

            // Title
            Label titleLabel = new Label("Confirmation");
            titleLabel.getStyleClass().add("dialog-alert");



            header.getChildren().addAll(iconPane, titleLabel);

            // Message
            Label messageLabel = new Label("Are you sure you want to purchase this item?\nCost: " + item.getPrice() + " points\nYour points: " + finalBuyer.getScoreTotal());
            messageLabel.getStyleClass().add("dialog-message");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(280);

            // Actions
            VBox actions = new VBox();
            actions.getStyleClass().add("dialog-actions");
            actions.setSpacing(8);
            actions.setAlignment(Pos.CENTER);

            Button confirmButton = new Button("I'm Sure");
            confirmButton.getStyleClass().add("dialog-read");
            confirmButton.setPrefWidth(280);
            confirmButton.setPrefHeight(40);

            Button cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().add("dialog-mark-as-read");
            cancelButton.setPrefWidth(280);
            cancelButton.setPrefHeight(40);

            actions.getChildren().addAll(confirmButton, cancelButton);

            dialogContent.getChildren().addAll(header, messageLabel, actions);

            // Set dialog content
            dialog.getDialogPane().setContent(dialogContent);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/store-cards.css").toExternalForm());

            // Handle button actions
            dialog.setResultConverter(dialogButton -> dialogButton == ButtonType.OK);
            confirmButton.setOnAction(e -> {
                dialog.setResult(true);
                dialog.close();
            });
            cancelButton.setOnAction(e -> {
                dialog.setResult(false);
                dialog.close();
            });


            // Show dialog and handle result
            Optional<Boolean> result = dialog.showAndWait();
            if (result.isPresent() && result.get()) {
                try {
                    int newScoreTotal = finalBuyer.getScoreTotal() - item.getPrice();
                    userService.updateScoreTotal(buyerId, newScoreTotal);

                    item.setStock(item.getStock() - 1);
                    storeItemService.update(item);

                    if (isTitleItem) {
                        UserTitle userTitle = new UserTitle(buyerId, item.getTitle().getId());
                        userTitleService.create(userTitle);
                    }

                    statusLabel.setText("Purchase successful!");
                    statusLabel.getStyleClass().remove("status-message-error");
                    statusLabel.getStyleClass().remove("status-message-info");
                    statusLabel.getStyleClass().add("status-message-ok");
                    buyButton.setDisable(true);

                    refreshGrid();
                } catch (Exception e) {
                    errorLabel.setText("Purchase failed: " + e.getMessage());
                    e.printStackTrace();
                    statusLabel.setText("Purchase failed!");
                    statusLabel.getStyleClass().add("status-message-error");
                }
            }
        });

        return card;
    }

    private Image getPlaceholderImage() {
        String placeholderPath = "/Images/placeholder.png";
        try {
            URL placeholderUrl = getClass().getResource(placeholderPath);
            if (placeholderUrl == null) {
                System.err.println("Placeholder image not found at: " + placeholderPath);
                // Fallback to a default JavaFX image or embedded resource
                return new Image("file:src/main/resources/Images/placeholder.png", 280, 140, true, true);
            }
            System.out.println("Loading placeholder image from: " + placeholderUrl.toExternalForm());
            return new Image(placeholderUrl.toExternalForm(), 280, 140, true, true);
        } catch (Exception e) {
            System.err.println("Error loading placeholder image: " + e.getMessage());
            // Ultimate fallback: create a blank image
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==", 280, 140, true, true);
        }
    }
}