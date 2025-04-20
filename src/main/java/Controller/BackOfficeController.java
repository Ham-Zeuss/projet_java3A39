package Controller;

import entite.Pack;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import service.PackService;

public class BackOfficeController {
    @FXML
    private TableView<Pack> packTable;
    @FXML
    private TableColumn<Pack, String> nameColumn;
    @FXML
    private TableColumn<Pack, Double> priceColumn;
    @FXML
    private TableColumn<Pack, String> featuresColumn;
    @FXML
    private TableColumn<Pack, Integer> validityPeriodColumn;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField featuresField;
    @FXML
    private TextField validityPeriodField;
    @FXML
    private Label errorLabel;

    private final PackService packService = new PackService();
    private final ObservableList<Pack> packList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        featuresColumn.setCellValueFactory(new PropertyValueFactory<>("features"));
        validityPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("validityPeriod"));
        packTable.setItems(packList);
        loadPacks();
        packTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        nameField.setText(newSelection.getName());
                        priceField.setText(String.valueOf(newSelection.getPrice()));
                        featuresField.setText(newSelection.getFeatures());
                        validityPeriodField.setText(String.valueOf(newSelection.getValidityPeriod()));
                    }
                }
        );
    }

    private void loadPacks() {
        try {
            packList.clear();
            packList.addAll(packService.getAllPacks());
            System.out.println("Loaded packs into table: " + packList.size());
            errorLabel.setText(packList.isEmpty() ? "No packs found" : "");
        } catch (Exception e) {
            errorLabel.setText("Error loading packs: " + e.getMessage());
            System.err.println("Load packs error: " + e.getMessage());
        }
    }

    private boolean validateInput(String name, String priceStr, String features, String validityStr) {
        if (name == null || name.trim().isEmpty()) {
            errorLabel.setText("Name cannot be empty");
            return false;
        }
        if (features == null || features.trim().isEmpty()) {
            errorLabel.setText("Features cannot be empty");
            return false;
        }
        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                errorLabel.setText("Price must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid price format");
            return false;
        }
        try {
            int validity = Integer.parseInt(validityStr);
            if (validity <= 0) {
                errorLabel.setText("Validity period must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid validity period format");
            return false;
        }
        return true;
    }

    @FXML
    private void addPack() {
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String features = featuresField.getText().trim();
        String validityStr = validityPeriodField.getText().trim();

        if (!validateInput(name, priceStr, features, validityStr)) {
            return;
        }

        try {
            Pack pack = new Pack(
                    0,
                    Double.parseDouble(priceStr),
                    features,
                    Integer.parseInt(validityStr),
                    name
            );
            packService.addPack(pack);
            loadPacks();
            clearFields();
            errorLabel.setText("Pack added successfully");
        } catch (Exception e) {
            errorLabel.setText("Error adding pack: " + e.getMessage());
            System.err.println("Add pack error: " + e.getMessage());
        }
    }

    @FXML
    private void updatePack() {
        Pack selected = packTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Select a pack to update");
            return;
        }

        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String features = featuresField.getText().trim();
        String validityStr = validityPeriodField.getText().trim();

        if (!validateInput(name, priceStr, features, validityStr)) {
            return;
        }

        try {
            Pack pack = new Pack(
                    selected.getId(),
                    Double.parseDouble(priceStr),
                    features,
                    Integer.parseInt(validityStr),
                    name
            );
            packService.updatePack(pack);
            loadPacks();
            clearFields();
            errorLabel.setText("Pack updated successfully");
        } catch (Exception e) {
            errorLabel.setText("Error updating pack: " + e.getMessage());
            System.err.println("Update pack error: " + e.getMessage());
        }
    }

    @FXML
    private void deletePack() {
        Pack selected = packTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Select a pack to delete");
            return;
        }
        try {
            packService.deletePack(selected.getId());
            loadPacks();
            clearFields();
            errorLabel.setText("Pack deleted successfully");
        } catch (Exception e) {
            errorLabel.setText("Error deleting pack: " + e.getMessage());
            System.err.println("Delete pack error: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        featuresField.clear();
        validityPeriodField.clear();
    }
}