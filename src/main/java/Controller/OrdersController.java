package Controller;

import entite.Commande;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import service.CommandeService;

public class OrdersController {
    @FXML
    private TableView<Commande> ordersTable;
    @FXML
    private TableColumn<Commande, Integer> idColumn;
    @FXML
    private TableColumn<Commande, Integer> userIdColumn;
    @FXML
    private TableColumn<Commande, Integer> packIdColumn;
    @FXML
    private TableColumn<Commande, Double> amountColumn;
    @FXML
    private TableColumn<Commande, String> dateColumn;
    @FXML
    private TableColumn<Commande, String> paymentMethodColumn;
    @FXML
    private TableColumn<Commande, String> expiryDateColumn;
    @FXML
    private TableColumn<Commande, String> statusColumn;
    @FXML
    private Label errorLabel;

    private final CommandeService commandeService = new CommandeService();
    private final ObservableList<Commande> commandeList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        packIdColumn.setCellValueFactory(new PropertyValueFactory<>("packId"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("commandeDate"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        ordersTable.setItems(commandeList);
        loadOrders();
    }

    private void loadOrders() {
        try {
            commandeList.clear();
            commandeList.addAll(commandeService.getAllCommandes());
            System.out.println("Loaded orders into table: " + commandeList.size());
            errorLabel.setText(commandeList.isEmpty() ? "No orders found" : "");
        } catch (Exception e) {
            errorLabel.setText("Error loading orders: " + e.getMessage());
            System.err.println("Load orders error: " + e.getMessage());
        }
    }
}