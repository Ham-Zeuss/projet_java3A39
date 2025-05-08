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
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
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
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.getStyleClass().add("icon-button");
        }
    }

    @FXML
    public void initialize() {
        pexelWordService = new PexelWordService();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        pexelWordsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null);
        });

        Platform.runLater(() -> {
            setupButton(createButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Create New Word");
            setupButton(updateButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Update Selected Word");
            setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete Selected Word");
        });

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

    private void loadFXML(Stage stage, String fxmlPathh) {
        try {
            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();

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
            Scene scene = new Scene(root, screenWidth, screenHeight);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(true);

            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading FXML: " + e.getMessage());
        }
    }
}