package Controller.Ham;

import entite.PexelWord;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.PexelWordService;

import java.util.function.Consumer;

public class PexelWordFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField wordField;

    @FXML
    private ComboBox<String> difficultyCombo;

    @FXML
    private Label errorLabel;

    private PexelWordService pexelWordService;
    private PexelWord pexelWord;
    private Consumer<Void> refreshCallback;
    private boolean isUpdateMode;

    public void initialize() {
        pexelWordService = new PexelWordService();

        // Populate the ComboBox with difficulty levels
        difficultyCombo.setItems(FXCollections.observableArrayList("easy", "hard"));
    }

    public void initData(PexelWord pexelWord, Consumer<Void> refreshCallback) {
        this.pexelWord = pexelWord;
        this.refreshCallback = refreshCallback;
        this.isUpdateMode = pexelWord != null;

        if (isUpdateMode) {
            titleLabel.setText("Update Pexel Word");
            wordField.setText(pexelWord.getWord());
            difficultyCombo.setValue(pexelWord.getDifficulty());
        } else {
            titleLabel.setText("Create Pexel Word");
            difficultyCombo.setValue("easy"); // Default value
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Validate inputs
            String word = wordField.getText().trim();
            String difficulty = difficultyCombo.getValue();

            if (word.isEmpty()) {
                errorLabel.setText("Word is required.");
                return;
            }

            if (difficulty == null) {
                errorLabel.setText("Difficulty is required.");
                return;
            }

            // Create or update the PexelWord
            if (isUpdateMode) {
                pexelWord.setWord(word);
                pexelWord.setDifficulty(difficulty);
                pexelWordService.update(pexelWord);
            } else {
                PexelWord newPexelWord = new PexelWord(word, difficulty);
                pexelWordService.createPst(newPexelWord);
            }

            // Refresh the table and close the window
            refreshCallback.accept(null);
            Stage stage = (Stage) wordField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            errorLabel.setText("Error saving pexel word: " + e.getMessage());
        }
    }
}