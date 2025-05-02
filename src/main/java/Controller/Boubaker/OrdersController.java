package Controller.Boubaker;

import entite.Commande;
import entite.Pack;
import entite.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import service.CommandeService;
import service.PackService;

import java.util.List;
import java.util.stream.Collectors;

public class OrdersController {
    @FXML
    private TableView<Commande> ordersTable;
    @FXML
    private TableColumn<Commande, String> packNameColumn;
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
    private final PackService packService = new PackService();
    private final ObservableList<Commande> commandeList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        packNameColumn.setCellValueFactory(cellData -> {
            Commande commande = cellData.getValue();
            Pack pack = packService.getPackById(commande.getPackId());
            return new javafx.beans.property.SimpleStringProperty(pack != null ? pack.getName() : "Unknown");
        });
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
            Session session = Session.getInstance();
            int userId = session.getUserId();
            if (!session.isActive() || userId <= 0) {
                errorLabel.setText("Please log in to view your orders.");
                System.err.println("Aucun utilisateur connecté pour charger les commandes.");
                return;
            }

            List<Commande> commandes = commandeService.getAllCommandes()
                    .stream()
                    .filter(c -> c.getUserId() == userId)
                    .collect(Collectors.toList());
            if (commandes != null && !commandes.isEmpty()) {
                commandeList.addAll(commandes);
                System.out.println("Loaded " + commandes.size() + " orders for userId=" + userId);
                errorLabel.setText("");
            } else {
                errorLabel.setText("No orders found.");
                System.out.println("Aucune commande trouvée pour userId=" + userId);
            }
        } catch (Exception e) {
            errorLabel.setText("Error loading orders: " + e.getMessage());
            System.err.println("Failed to load orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
}