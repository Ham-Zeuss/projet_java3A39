package Controller;

import entite.Consultation;
import entite.Profile;
import entite.User;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import service.ConsultationService;
import service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserConsultationsController {

    @FXML
    private TableView<Consultation> consultationsTable;

    @FXML
    private TableColumn<Consultation, String> dateColumn;

    @FXML
    private TableColumn<Consultation, String> timeColumn;

    @FXML
    private TableColumn<Consultation, String> profileColumn;

    @FXML
    private TableColumn<Consultation, Boolean> completedColumn;

    @FXML
    private Button closeButton;

    private ConsultationService consultationService;
    private UserService userService;

    private final int FIXED_USER_ID = 1; // Fixed user ID for now

    public void initialize() {
        try {
            consultationService = new ConsultationService();
            userService = new UserService();

            // Set up table columns
            dateColumn.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getConsultationDate();
                return new SimpleStringProperty(dateTime != null ?
                        dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            });

            timeColumn.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getConsultationDate();
                return new SimpleStringProperty(dateTime != null ?
                        dateTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "");
            });

            profileColumn.setCellValueFactory(cellData -> {
                Profile profile = cellData.getValue().getProfileId();
                if (profile != null && profile.getUserId() != null) {
                    User profileUser = profile.getUserId();
                    return new SimpleStringProperty(
                            profileUser.getPrenom() + " " + profileUser.getNom() + " (" + profile.getSpecialite() + ")");
                }
                return new SimpleStringProperty("Unknown");
            });

            completedColumn.setCellValueFactory(cellData ->
                    new SimpleBooleanProperty(cellData.getValue().isCompleted()));

            // Fetch and display consultations for user with ID = 1
            User user = userService.readById(FIXED_USER_ID);
            if (user != null) {
                ObservableList<Consultation> consultations =
                        FXCollections.observableArrayList(consultationService.readByUserId(FIXED_USER_ID));
                consultationsTable.setItems(consultations);
            } else {
                consultationsTable.setPlaceholder(new Label("No user found with ID: " + FIXED_USER_ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
            consultationsTable.setPlaceholder(new Label("Error loading consultations: " + e.getMessage()));
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
