package Controller;

import entite.StoreItem;
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
import javafx.scene.layout.HBox;
import service.StoreItemService;
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
    private ObservableList<StoreItem> storeItemsList;
    private static final int BUYER_ID = 14;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storeItemService = new StoreItemService();
        userService = new UserService();
        userTitleService = new UserTitleService();
        storeItemsList = FXCollections.observableArrayList();

        // Initialize Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(cellData -> {
            StoreItem item = cellData.getValue();
            return new SimpleStringProperty(item.getTitle() != null ? item.getTitle().getName() : "N/A");
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
                        errorLabel.setText("Update functionality not implemented.");
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
                    } else if (buyer == null || buyer.getPoints() == null || buyer.getPoints() < storeItem.getPrice()) {
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
                                int newPoints = buyer.getPoints() - storeItem.getPrice();
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

    private void refreshTable() {
        storeItemsList.clear();
        storeItemsList.addAll(storeItemService.readAll());
        storeItemsTable.setItems(storeItemsList);
    }
}