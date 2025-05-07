package Controller.Boubaker;

import entite.Commande;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import service.CommandeService;

import java.io.IOException;

public class ViewOrdersController {

    @FXML private TableView<Commande> ordersTable;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, Integer> userIdColumn;
    @FXML private TableColumn<Commande, Integer> packIdColumn;
    @FXML private TableColumn<Commande, Double> amountColumn;
    @FXML private TableColumn<Commande, String> dateColumn;
    @FXML private TableColumn<Commande, String> paymentMethodColumn;
    @FXML private TableColumn<Commande, String> expiryDateColumn;
    @FXML private TableColumn<Commande, String> statusColumn;
    @FXML private TableColumn<Commande, Void> actionColumn;
    @FXML private Label errorLabel;

    private final CommandeService commandeService = new CommandeService();
    private final ObservableList<Commande> commandes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        packIdColumn.setCellValueFactory(new PropertyValueFactory<>("packId"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("commandeDate"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                setupButton(editButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Edit", true);
                setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete", true);

                editButton.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    System.out.println("Edit order: " + commande.getId());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Boubaker/update_order.fxml"));
                        if (loader.getLocation() == null) {
                            System.out.println("Error: /Boubaker/update_order.fxml not found");
                            errorLabel.setText("Error: Update order FXML not found");
                            return;
                        }
                        Parent root = loader.load();
                        UpdateOrderController controller = loader.getController();
                        controller.loadOrderData(commande);
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Update Order");
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorLabel.setText("Error opening update order: " + e.getMessage());
                    }
                });

                deleteButton.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    try {
                        System.out.println("Deleting order with ID: " + commande.getId());
                        commandeService.deleteCommande(commande.getId());
                        loadOrders();
                        errorLabel.setText("Order deleted successfully");
                    } catch (RuntimeException e) {
                        errorLabel.setText("Error deleting order: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(10, editButton, deleteButton));
                }
            }
        });

        ordersTable.setItems(commandes);
        loadOrders();
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            // Show text only if showText is true
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 120 : 60, 60); // Larger width for buttons with text
            // Apply specified style
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 120 : 60, 60);
            // Apply same style in fallback case
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        }
    }

    private void loadOrders() {
        try {
            commandes.clear();
            commandes.addAll(commandeService.getAllCommandes());
            errorLabel.setText("");
        } catch (RuntimeException e) {
            errorLabel.setText("Error loading orders: " + e.getMessage());
        }
    }
}
