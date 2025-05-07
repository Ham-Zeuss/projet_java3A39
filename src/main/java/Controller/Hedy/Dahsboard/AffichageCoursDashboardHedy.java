package Controller.Hedy.Dahsboard;

import Controller.Hedy.*;
import entite.Cours;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CoursService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AffichageCoursDashboardHedy {

    @FXML private Label moduleTitleLabel;
    @FXML private Label courseCountLabel;

    @FXML private TableView<Cours> coursTable;
    @FXML private TableColumn<Cours, String> titleCol;
    @FXML private TableColumn<Cours, String> pdfCol;
    @FXML private TableColumn<Cours, String> updatedAtCol;
    @FXML private TableColumn<Cours, Void> actionsCol;

    private Module currentModule;
    private final CoursService coursService = new CoursService();

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            moduleTitleLabel.setText("Module: " + module.getTitle());
            loadCoursCards();

            // Configure addButton with icon
            Platform.runLater(() -> {
                Button addButton = (Button) coursTable.getScene().lookup("#addButton");
                if (addButton != null) {
                    setupButton(addButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Add Course");
                }
            });
        }
    }

    public void loadCoursCards() {
        coursTable.getItems().clear();

        List<Cours> coursList = coursService.getCoursByModule(currentModule.getId());

        // Update course count label safely
        if (courseCountLabel != null) {
            courseCountLabel.setText("Nombre de cours : " + coursList.size());
        }

        // Set up columns using lambda expressions
        titleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        pdfCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPdfName()));

        updatedAtCol.setCellValueFactory(cellData -> {
            Cours cours = cellData.getValue();
            if (cours.getUpdatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cours.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            } else {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        // Action buttons column
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                setupButton(editButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Edit Course");
                setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete Course");

                editButton.setOnAction(event -> {
                    Cours cours = getTableRow().getItem();
                    if (cours != null) {
                        editCours(cours);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Cours cours = getTableRow().getItem();
                    if (cours != null) {
                        coursService.delete(cours);
                        loadCoursCards();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, editButton, deleteButton));
                }
            }
        });

        // Populate table
        coursTable.getItems().addAll(coursList);
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

    @FXML
    private void retourModules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageModule.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coursTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Modules");
        } catch (IOException e) {
            System.err.println("Error loading AffichageModule.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void goToAjoutCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AjoutCours.fxml"));
            Parent root = loader.load();

            AjoutCoursController controller = loader.getController();
            controller.setCurrentModule(currentModule);

            Stage stage = (Stage) coursTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Cours");
        } catch (IOException e) {
            System.err.println("Error loading AjoutCours.fxml: " + e.getMessage());
        }
    }

    private void editCours(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/EditCours.fxml"));
            Parent root = loader.load();
            EditCoursController controller = loader.getController();
            controller.setCoursToEdit(cours);
            Stage stage = (Stage) coursTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier un Cours");
        } catch (IOException e) {
            System.err.println("Error loading EditCours.fxml: " + e.getMessage());
        }
    }
}