package Controller.Hedy.Dahsboard;

import entite.Module;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.ModuleService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AffichageModuleDashboardController {

    @FXML private TableView<Module> modulesTable;
    @FXML private TableColumn<Module, String> titleColumn;
    @FXML private TableColumn<Module, String> descriptionColumn;
    @FXML private TableColumn<Module, Integer> coursesColumn;
    @FXML private TableColumn<Module, String> levelColumn;
    @FXML private TextField searchField;

    private final ModuleService moduleService = new ModuleService();
    private Integer loggedInUserId;

    public void setLoggedInUserId(Integer userId) {
        this.loggedInUserId = userId;
    }

    @FXML
    public void initialize() {
        configureTable();
        loadModules();

        // Add listener to the search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterModules(newValue);
        });

        // Clear focus from the search bar when the page loads
        Platform.runLater(() -> {
            searchField.getParent().requestFocus();
        });

        // Add listener to open AffichageCours when a module title is clicked
        titleColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    setOnMouseClicked(event -> {
                        Module module = getTableView().getItems().get(getIndex());
                        if (module != null) {
                            showModuleCourses(module);
                        }
                    });
                }
            }
        });
    }

    @FXML
    private void openAjoutPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AjoutModule.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initOwner(modulesTable.getScene().getWindow());
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("Ajouter un Module");
            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            AjoutModuleController controller = loader.getController();
            controller.setPopupStage(popupStage);

            popupStage.showAndWait();
            loadModules();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureTable() {
        // Clear existing columns to avoid duplicate column errors
        modulesTable.getColumns().clear();

        // Configure each column
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        coursesColumn.setCellValueFactory(cellData -> cellData.getValue().nombreCoursProperty().asObject());
        levelColumn.setCellValueFactory(cellData -> cellData.getValue().levelProperty());

        // Add "Actions" column with buttons
        TableColumn<Module, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setPrefWidth(220);

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                setupButton(editButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Edit Module");
                setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete Module");

                editButton.setOnAction(event -> {
                    Module module = getTableView().getItems().get(getIndex());
                    if (module != null) {
                        editModule(module);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Module module = getTableView().getItems().get(getIndex());
                    if (module != null) {
                        deleteModule(module);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(10);
                    buttons.getChildren().addAll(editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        // Add all columns to the table
        modulesTable.getColumns().addAll(titleColumn, descriptionColumn, coursesColumn, levelColumn, actionColumn);

        // Configure addButton with icon
        Platform.runLater(() -> {
            Button addButton = (Button) modulesTable.getScene().lookup("#addButton");
            if (addButton != null) {
                setupButton(addButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Add Module");
            }
        });
    }

    private void setupButton(Button button, String iconUrl, String tooltipText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            button.setText("");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.getStyleClass().add("icon-button");
        }
    }

    private void deleteModule(Module module) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le module");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce module ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                moduleService.delete(module);
                loadModules();
            }
        });
    }

    private void editModule(Module module) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/EditModule.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initOwner(modulesTable.getScene().getWindow());
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("Modifier Module");
            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            EditModuleController controller = loader.getController();
            controller.setModuleToEdit(module);

            popupStage.showAndWait();
            loadModules();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadModules() {
        List<Module> modules = moduleService.readAll();
        modulesTable.getItems().setAll(modules);
    }

    private void showModuleCourses(Module module) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
            Parent root = loader.load();

            AffichageCoursDashboardHedy controller = loader.getController();
            controller.setModule(module);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + module.getTitle());
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading AffichageCoursDashboard.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void goToAjoutPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/HedyFXML/AjoutModule.fxml"));
            Stage stage = (Stage) modulesTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Module");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterModules(String searchText) {
        List<Module> allModules = moduleService.readAll();
        List<Module> filteredModules = allModules.stream()
                .filter(module -> module.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
        modulesTable.getItems().setAll(filteredModules);
    }
}