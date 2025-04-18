package Controller;

import entite.StoreItem;
import entite.Title;
import entite.User;
import entite.UserTitle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import service.StoreItemService;
import service.TitleService;
import service.UserService;
import service.UserTitleService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListStoreItemsController implements Initializable {

    @FXML
    private TableView<StoreItem> storeItemsTable;

    @FXML
    private TableColumn<StoreItem, Integer> idColumn;

    @FXML
    private TableColumn<StoreItem, String> titleColumn;

    @FXML
    private TableColumn<StoreItem, String> nameColumn;

    @FXML
    private TableColumn<StoreItem, String> descriptionColumn;

    @FXML
    private TableColumn<StoreItem, Integer> priceColumn;

    @FXML
    private TableColumn<StoreItem, String> imageColumn;

    @FXML
    private TableColumn<StoreItem, Integer> stockColumn;

    @FXML
    private TableColumn<StoreItem, Void> updateColumn;

    @FXML
    private TableColumn<StoreItem, Void> deleteColumn;

    @FXML
    private TableColumn<StoreItem, Void> buyColumn;

    @FXML
    private Label errorLabel;

    private StoreItemService storeItemService;
    private UserService userService;
    private UserTitleService userTitleService;
    private TitleService titleService;
    private ObservableList<StoreItem> storeItemsList;
    private static final int BUYER_ID = 14;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storeItemService = new StoreItemService();
        userService = new UserService();
        userTitleService = new UserTitleService();
        titleService = new TitleService();
        storeItemsList = FXCollections.observableArrayList();

        // Initialize Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(cellData -> {
            StoreItem item = cellData.getValue();
            boolean isTitleItem = item.getTitle() != null && item.getTitle().getId() != 0;
            return new SimpleStringProperty(isTitleItem ? "Title item." : "Not a title item.");
        });
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Update Column
        updateColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    updateButton.setOnAction(event -> {
                        StoreItem storeItem = getTableView().getItems().get(getIndex());
                        Dialog<StoreItem> dialog = createUpdateDialog(storeItem);
                        Optional<StoreItem> result = dialog.showAndWait();
                        result.ifPresent(updatedItem -> {
                            try {
                                storeItemService.update(updatedItem);
                                refreshTable();
                                errorLabel.setText("Item updated successfully.");
                            } catch (Exception e) {
                                errorLabel.setText("Update failed: " + e.getMessage());
                            }
                        });
                    });
                    setGraphic(updateButton);
                    setText(null);
                }
            }
        });

        // Delete Column
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    deleteButton.setOnAction(event -> {
                        StoreItem storeItem = getTableView().getItems().get(getIndex());
                        storeItemService.delete(storeItem);
                        storeItemsList.remove(storeItem);
                        errorLabel.setText("Item deleted successfully.");
                    });
                    setGraphic(deleteButton);
                    setText(null);
                }
            }
        });

        // Buy Column
        buyColumn.setCellFactory(param -> new TableCell<>() {
            private final Button buyButton = new Button("Buy");
            private final Label messageLabel = new Label();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StoreItem storeItem = getTableView().getItems().get(getIndex());
                    User buyer = userService.readById(BUYER_ID);
                    HBox hbox = new HBox(5, buyButton, messageLabel);
                    HBox.setMargin(buyButton, new Insets(0, 5, 0, 0));
                    HBox.setMargin(messageLabel, new Insets(0, 5, 0, 0));

                    // Check conditions for enabling/disabling the Buy button
                    boolean isTitleItem = storeItem.getTitle() != null && storeItem.getTitle().getId() != 0;
                    boolean alreadyOwned = isTitleItem && userTitleService.userOwnsTitle(BUYER_ID, storeItem.getTitle().getId());

                    if (alreadyOwned) {
                        buyButton.setDisable(true);
                        messageLabel.setText("Already owned");
                        messageLabel.setStyle("-fx-text-fill: red;");
                    } else if (storeItem.getStock() <= 0) {
                        buyButton.setDisable(true);
                        messageLabel.setText("Out of stock");
                        messageLabel.setStyle("-fx-text-fill: red;");
                    } else if (buyer == null || buyer.getScoreTotal() == null || buyer.getScoreTotal() < storeItem.getPrice()) {
                        buyButton.setDisable(true);
                        messageLabel.setText("Insufficient points");
                        messageLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        buyButton.setDisable(false);
                        messageLabel.setText("");
                    }

                    buyButton.setOnAction(event -> {
                        // Confirm purchase
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Purchase");
                        alert.setHeaderText("Purchase Item: " + storeItem.getName());
                        alert.setContentText("Price: " + storeItem.getPrice() + " points\nDo you want to proceed?");
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                // Update user points
                                int newPoints = buyer.getScoreTotal() - storeItem.getPrice();
                                userService.updatePoints(BUYER_ID, newPoints);

                                // Update item stock
                                storeItem.setStock(storeItem.getStock() - 1);
                                storeItemService.update(storeItem);

                                // Record title ownership if applicable
                                if (isTitleItem) {
                                    UserTitle userTitle = new UserTitle(BUYER_ID, storeItem.getTitle().getId());
                                    userTitleService.create(userTitle);
                                }

                                // Refresh table
                                refreshTable();

                                errorLabel.setText("Purchase successful!");
                            } catch (Exception e) {
                                errorLabel.setText("Purchase failed: " + e.getMessage());
                            }
                        }
                    });

                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Load data
        refreshTable();
    }

    private Dialog<StoreItem> createUpdateDialog(StoreItem storeItem) {
        Dialog<StoreItem> dialog = new Dialog<>();
        dialog.setTitle("Update Store Item");
        dialog.setHeaderText("Edit item: " + storeItem.getName());

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(storeItem.getName());
        nameField.setPromptText("Name");
        TextArea descriptionArea = new TextArea(storeItem.getDescription());
        descriptionArea.setPromptText("Description");
        TextField priceField = new TextField(String.valueOf(storeItem.getPrice()));
        priceField.setPromptText("Price");
        TextField imageField = new TextField(storeItem.getImage());
        imageField.setPromptText("Image URL");
        TextField stockField = new TextField(String.valueOf(storeItem.getStock()));
        stockField.setPromptText("Stock");

        // ComboBox for titles
        ComboBox<Title> titleComboBox = new ComboBox<>();
        ObservableList<Title> titleOptions = FXCollections.observableArrayList();
        titleOptions.add(null); // Option for no title
        titleOptions.addAll(titleService.readAll());
        titleComboBox.setItems(titleOptions);
        titleComboBox.setPromptText("Select Title (optional)");
        // Set current title if exists
        titleComboBox.setValue(storeItem.getTitle() != null && storeItem.getTitle().getId() != 0 ? storeItem.getTitle() : null);
        // Custom string converter to show title name or "None"
        titleComboBox.setConverter(new javafx.util.StringConverter<Title>() {
            @Override
            public String toString(Title title) {
                return title == null ? "None" : title.getName();
            }

            @Override
            public Title fromString(String string) {
                return titleOptions.stream()
                        .filter(t -> t != null && t.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Image URL:"), 0, 3);
        grid.add(imageField, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Title:"), 0, 5);
        grid.add(titleComboBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Enable Save button only if inputs are valid
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || !isValidInput(priceField, stockField));
        });
        priceField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() || !isValidInput(priceField, stockField));
        });
        stockField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() || !isValidInput(priceField, stockField));
        });

        // Convert dialog result to StoreItem
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new StoreItem(
                            storeItem.getId(),
                            titleComboBox.getValue(),
                            nameField.getText(),
                            descriptionArea.getText().isEmpty() ? null : descriptionArea.getText(),
                            Integer.parseInt(priceField.getText()),
                            imageField.getText().isEmpty() ? null : imageField.getText(),
                            Integer.parseInt(stockField.getText())
                    );
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid number format for price or stock.");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private boolean isValidInput(TextField priceField, TextField stockField) {
        try {
            int price = Integer.parseInt(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            return price >= 0 && stock >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void refreshTable() {
        storeItemsList.clear();
        storeItemsList.addAll(storeItemService.readAll());
        storeItemsTable.setItems(storeItemsList);
    }
}