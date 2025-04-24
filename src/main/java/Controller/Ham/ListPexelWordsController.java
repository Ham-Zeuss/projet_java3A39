package Controller.Ham;

import entite.PexelWord;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import service.PexelWordService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ListPexelWordsController {

    @FXML
    private TableView<PexelWord> pexelWordsTable;

    @FXML
    private TableColumn<PexelWord, Integer> idColumn;

    @FXML
    private TableColumn<PexelWord, String> wordColumn;

    @FXML
    private TableColumn<PexelWord, String> difficultyColumn;

    @FXML
    private Button createButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label errorLabel;

    private PexelWordService pexelWordService;

    @FXML
    public void initialize() {
        // Initialize PexelWordService
        pexelWordService = new PexelWordService();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        // Enable/disable buttons based on selection
        pexelWordsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null);
        });

        // Load data into table
        loadPexelWords();

        // Optional: Add sidebar in controller (uncomment if needed)
        /*
        Stage stage = (Stage) pexelWordsTable.getScene().getWindow();
        test.Sidebar sidebarCreator = new test.Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
            stage,
            () -> loadFXML(stage, "/test/Dashboard.fxml"), // Adjust path if Dashboard is FXML
            () -> loadFXML(stage, "/User/index_user.fxml"),
            () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
            () -> System.out.println("Logout clicked")
        );
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(pexelWordsTable.getScene().getRoot());
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        stage.setScene(scene);
        */
    }

    private void loadPexelWords() {
        try {
            ObservableList<PexelWord> words = FXCollections.observableArrayList(pexelWordService.readAll());
            pexelWordsTable.setItems(words);
            errorLabel.setText("");
        } catch (Exception e) {
            errorLabel.setText("Error loading pexel words: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreate() {
        openPexelWordForm(null);
    }

    @FXML
    private void handleUpdate() {
        PexelWord selectedWord = pexelWordsTable.getSelectionModel().getSelectedItem();
        if (selectedWord != null) {
            openPexelWordForm(selectedWord);
        }
    }

    @FXML
    private void handleDelete() {
        PexelWord selectedWord = pexelWordsTable.getSelectionModel().getSelectedItem();
        if (selectedWord != null) {
            try {
                pexelWordService.delete(selectedWord);
                loadPexelWords();
                errorLabel.setText("Pexel word deleted successfully!");
            } catch (Exception e) {
                errorLabel.setText("Error deleting pexel word: " + e.getMessage());
            }
        }
    }

    private void openPexelWordForm(PexelWord pexelWord) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HamzaFXML/PexelWordForm.fxml"));
            Parent root = loader.load();

            PexelWordFormController controller = loader.getController();
            controller.initData(pexelWord, v -> loadPexelWords());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(pexelWord == null ? "Create Pexel Word" : "Update Pexel Word");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            errorLabel.setText("Error opening form: " + e.getMessage());
        }
    }



    private void loadFXML(Stage stage, String fxmlPathh) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPathh));
            Parent fxmlContent = loader.load();
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");
            test.Sidebar sidebarCreator = new test.Sidebar();
            ScrollPane sidebar = sidebarCreator.createSidebar(
                    stage,
                    () -> loadFXML(stage, "/test/Dashboard.fxml"), // Dashboard action
                    () -> loadFXML(stage, "/User/index_user.fxml"), // Utilisateurs action
                    () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"), // Pixel Words action
                    () -> System.out.println("Logout clicked"), // Logout action

                    fxmlPath -> loadFXML(stage, fxmlPathh) // Consumer for loadFXML
            );
            root.setLeft(sidebar);
            root.setCenter(fxmlContent);
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            errorLabel.setText("Error loading FXML: " + e.getMessage());
        }
    }

}