package Controller.Maryem;

import entite.Consultation;
import entite.User;
import entite.Profile;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ConsultationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DisplayConsultationsController {

    @FXML
    private TableView<Consultation> consultationsTable;

    @FXML
    private TableColumn<Consultation, Number> idColumn;

    @FXML
    private TableColumn<Consultation, String> userNameColumn;

    @FXML
    private TableColumn<Consultation, String> doctorNameColumn;

    @FXML
    private TableColumn<Consultation, String> profileSpecialtyColumn;

    @FXML
    private TableColumn<Consultation, String> consultationDateColumn;

    @FXML
    private TableColumn<Consultation, Boolean> isCompletedColumn;

    @FXML
    private TableColumn<Consultation, Void> deleteColumn;

    @FXML
    private TableColumn<Consultation, Void> editColumn;

    @FXML
    private Button addButton; // Nouveau bouton

    @FXML
    private Label errorLabel;

    private ConsultationService consultationService;

    @FXML
    public void initialize() {
        System.out.println("Entering DisplayConsultationsController.initialize");
        try {
            consultationService = new ConsultationService();
            System.out.println("ConsultationService initialized");

            // Configure table columns
            idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));

            userNameColumn.setCellValueFactory(cellData -> {
                User user = cellData.getValue().getUserId();
                String name = user != null ? user.getPrenom() + " " + user.getNom() : "Unknown";
                return new SimpleStringProperty(name);
            });

            doctorNameColumn.setCellValueFactory(cellData -> {
                Profile profile = cellData.getValue().getProfileId();
                User doctor = profile != null ? profile.getUserId() : null;
                String doctorName = doctor != null ? doctor.getPrenom() + " " + doctor.getNom() : "Unknown";
                return new SimpleStringProperty(doctorName);
            });

            profileSpecialtyColumn.setCellValueFactory(cellData -> {
                Profile profile = cellData.getValue().getProfileId();
                String specialty = profile != null ? profile.getSpecialite() : "Unknown";
                return new SimpleStringProperty(specialty);
            });

            consultationDateColumn.setCellValueFactory(cellData -> {
                LocalDateTime date = cellData.getValue().getConsultationDate();
                String formattedDate = date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
                return new SimpleStringProperty(formattedDate);
            });

            isCompletedColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isCompleted()));

            // Configure delete column with button
            deleteColumn.setCellFactory(param -> new TableCell<>() {
                private final Button deleteButton = new Button("Delete");

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Consultation consultation = getTableView().getItems().get(getIndex());
                        deleteButton.setOnAction(event -> deleteConsultation(consultation));
                        setGraphic(deleteButton);
                    }
                }
            });

            // Configure edit column with button
            editColumn.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button("Edit");

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Consultation consultation = getTableView().getItems().get(getIndex());
                        editButton.setOnAction(event -> openEditWindow(consultation));
                        setGraphic(editButton);
                    }
                }
            });

            // Load consultations
            consultationsTable.getItems().setAll(consultationService.readAll());
            System.out.println("Consultations loaded: " + consultationsTable.getItems().size());

            if (consultationsTable.getItems().isEmpty()) {
                errorLabel.setText("No consultations found in the database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading consultations: " + e.getMessage());
        }
        System.out.println("Exiting DisplayConsultationsController.initialize");
    }

    private void deleteConsultation(Consultation consultation) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to delete the consultation with ID " +
                    consultation.getId() + "?");

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                consultationService.delete(consultation);
                consultationsTable.getItems().remove(consultation);
                System.out.println("Consultation deleted: ID " + consultation.getId());

                if (consultationsTable.getItems().isEmpty()) {
                    errorLabel.setText("No consultations found in the database.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error deleting consultation", "Could not delete consultation: " + e.getMessage());
        }
    }

    private void openEditWindow(Consultation consultation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/UpdateConsultation.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Update Consultation");

            stage.initModality(Modality.APPLICATION_MODAL);

            UpdateConsultationController controller = loader.getController();
            controller.setConsultation(consultation, this);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open edit window: " + e.getMessage());
        }
    }

    @FXML
    private void openAddWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/AddConsultation.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add Consultation");

            stage.initModality(Modality.APPLICATION_MODAL);

            AddConsultationController controller = loader.getController();
            controller.initialize(this);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open add window: " + e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            consultationsTable.getItems().setAll(consultationService.readAll());
            if (consultationsTable.getItems().isEmpty()) {
                errorLabel.setText("No consultations found in the database.");
            } else {
                errorLabel.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error refreshing table: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}