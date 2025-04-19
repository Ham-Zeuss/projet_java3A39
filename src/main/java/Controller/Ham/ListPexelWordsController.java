package Controller.Ham;

import entite.PexelWord;
import service.PexelWordService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
}