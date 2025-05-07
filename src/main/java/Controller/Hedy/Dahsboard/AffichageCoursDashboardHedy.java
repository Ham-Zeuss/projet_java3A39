package Controller.Hedy.Dahsboard;
import Controller.Hedy.*;
import entite.Cours;
import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
            loadCoursCards(); // Load courses into table
        }
    }

    public void loadCoursCards() {
        coursTable.getItems().clear(); // Clear existing items

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
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

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
                        loadCoursCards(); // Refresh table
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

        // Populate table
        coursTable.getItems().addAll(coursList);
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