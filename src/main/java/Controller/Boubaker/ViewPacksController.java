package Controller.Boubaker;

import entite.Pack;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import service.PackService;

import java.io.IOException;
import java.sql.SQLException;

public class ViewPacksController {

    @FXML private TableView<Pack> packsTable;
    @FXML private TableColumn<Pack, Integer> idColumn;
    @FXML private TableColumn<Pack, String> nameColumn;
    @FXML private TableColumn<Pack, Double> priceColumn;
    @FXML private TableColumn<Pack, Integer> validityColumn;
    @FXML private TableColumn<Pack, String> featuresColumn;
    @FXML private TableColumn<Pack, Void> actionColumn;
    @FXML private Label errorLabel;

    private final PackService packService = new PackService();
    private final ObservableList<Pack> packs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        validityColumn.setCellValueFactory(new PropertyValueFactory<>("validityPeriod"));
        featuresColumn.setCellValueFactory(new PropertyValueFactory<>("features"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: #C20114; -fx-text-fill: #FFFFFF; -fx-font-family: Arial; -fx-font-size: 12; -fx-background-radius: 5;");
                deleteButton.setStyle("-fx-background-color: #C20114; -fx-text-fill: #FFFFFF; -fx-font-family: Arial; -fx-font-size: 12; -fx-background-radius: 5;");

                editButton.setOnAction(event -> {
                    Pack pack = getTableView().getItems().get(getIndex());
                    System.out.println("Edit pack: " + pack.getId());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Boubaker/update_pack.fxml"));
                        if (loader.getLocation() == null) {
                            System.out.println("Error: /Boubaker/update_pack.fxml not found");
                            errorLabel.setText("Error: Update pack FXML not found");
                            return;
                        }
                        Parent root = loader.load();
                        UpdatePackController controller = loader.getController();
                        controller.loadPackData(pack);
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Update Pack");
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorLabel.setText("Error opening update pack: " + e.getMessage());
                    }
                });

                deleteButton.setOnAction(event -> {
                    Pack pack = getTableView().getItems().get(getIndex());
                    try {
                        packService.deletePack(pack.getId());
                        loadPacks();
                        errorLabel.setText("Pack deleted successfully");
                    } catch (RuntimeException e) {
                        errorLabel.setText("Database error: " + e.getMessage());
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

        packsTable.setItems(packs);
        loadPacks();
    }

    private void loadPacks() {
        try {
            packs.clear();
            packs.addAll(packService.getAllPacks());
            errorLabel.setText("");
        } catch (RuntimeException e) {
            errorLabel.setText("Error loading packs: " + e.getMessage());
        }
    }
}