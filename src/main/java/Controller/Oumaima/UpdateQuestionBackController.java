package controller.Oumaima;

import entite.Oumaima.Question;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.Oumaima.QuestionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateQuestionBackController {

    @FXML
    private TextArea questionTextArea;

    @FXML
    private RadioButton radioType;

    @FXML
    private RadioButton checkboxType;

    @FXML
    private ToggleGroup questionTypeGroup;

    @FXML
    private TextField option1Field;

    @FXML
    private TextField option2Field;

    @FXML
    private TextField option3Field;

    @FXML
    private TextField option4Field;

    @FXML
    private CheckBox correctOption1;

    @FXML
    private CheckBox correctOption2;

    @FXML
    private CheckBox correctOption3;

    @FXML
    private CheckBox correctOption4;

    @FXML
    private Button updateQuestionButton;

    @FXML
    private Button cancelButton;

    private final QuestionService questionService = new QuestionService();
    private Question questionToUpdate;
    private int quizId;

    // Listeners pour gérer la restriction d'une seule case cochée en mode "RadioButton"
    private ChangeListener<Boolean> correctOption1Listener;
    private ChangeListener<Boolean> correctOption2Listener;
    private ChangeListener<Boolean> correctOption3Listener;
    private ChangeListener<Boolean> correctOption4Listener;

    // Méthode pour initialiser la question à modifier
    public void setQuestionToUpdate(Question question, int quizId) {
        this.questionToUpdate = question;
        this.quizId = quizId;
        populateFields();
        setupQuestionTypeListener();
    }

    private void populateFields() {
        // Remplir les champs avec les données de la question
        questionTextArea.setText(questionToUpdate.getText());

        // Type de question
        String optionType = questionToUpdate.getOptionType() != null ? questionToUpdate.getOptionType().toLowerCase() : "";
        if ("radio".equals(optionType) || "radiobox".equals(optionType)) {
            radioType.setSelected(true);
        } else if ("checkbox".equals(optionType)) {
            checkboxType.setSelected(true);
        }

        // Options
        option1Field.setText(questionToUpdate.getOption1());
        option2Field.setText(questionToUpdate.getOption2());
        option3Field.setText(questionToUpdate.getOption3());
        option4Field.setText(questionToUpdate.getOption4());

        // Réponses correctes
        String correctAnswers = questionToUpdate.getCorrectAnswers() != null ? questionToUpdate.getCorrectAnswers() : "[]";
        List<String> correctOptions = parseCorrectAnswers(correctAnswers);
        correctOption1.setSelected(correctOptions.contains(option1Field.getText()));
        correctOption2.setSelected(correctOptions.contains(option2Field.getText()));
        correctOption3.setSelected(correctOptions.contains(option3Field.getText()));
        correctOption4.setSelected(correctOptions.contains(option4Field.getText()));
    }

    private void setupQuestionTypeListener() {
        // Listener pour gérer le changement de type de question
        radioType.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // Mode "RadioButton" : une seule case cochée autorisée
                enableSingleSelectionMode();
            }
        });

        checkboxType.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // Mode "CheckBox" : plusieurs cases cochées autorisées
                disableSingleSelectionMode();
            }
        });

        // Appliquer la restriction immédiatement si "RadioButton" est déjà sélectionné
        if (radioType.isSelected()) {
            enableSingleSelectionMode();
        }
    }

    private void enableSingleSelectionMode() {
        // Supprimer les anciens listeners pour éviter les duplications
        disableSingleSelectionMode();

        // Créer des listeners spécifiques pour chaque CheckBox
        correctOption1Listener = (obs, oldValue, newValue) -> {
            if (newValue) { // La case 1 est cochée
                correctOption2.setSelected(false);
                correctOption3.setSelected(false);
                correctOption4.setSelected(false);
            }
        };

        correctOption2Listener = (obs, oldValue, newValue) -> {
            if (newValue) { // La case 2 est cochée
                correctOption1.setSelected(false);
                correctOption3.setSelected(false);
                correctOption4.setSelected(false);
            }
        };

        correctOption3Listener = (obs, oldValue, newValue) -> {
            if (newValue) { // La case 3 est cochée
                correctOption1.setSelected(false);
                correctOption2.setSelected(false);
                correctOption4.setSelected(false);
            }
        };

        correctOption4Listener = (obs, oldValue, newValue) -> {
            if (newValue) { // La case 4 est cochée
                correctOption1.setSelected(false);
                correctOption2.setSelected(false);
                correctOption3.setSelected(false);
            }
        };

        // Ajouter les listeners à chaque case à cocher
        correctOption1.selectedProperty().addListener(correctOption1Listener);
        correctOption2.selectedProperty().addListener(correctOption2Listener);
        correctOption3.selectedProperty().addListener(correctOption3Listener);
        correctOption4.selectedProperty().addListener(correctOption4Listener);
    }

    private void disableSingleSelectionMode() {
        // Supprimer les listeners pour permettre plusieurs sélections
        if (correctOption1Listener != null) {
            correctOption1.selectedProperty().removeListener(correctOption1Listener);
            correctOption2.selectedProperty().removeListener(correctOption2Listener);
            correctOption3.selectedProperty().removeListener(correctOption3Listener);
            correctOption4.selectedProperty().removeListener(correctOption4Listener);
            correctOption1Listener = null;
            correctOption2Listener = null;
            correctOption3Listener = null;
            correctOption4Listener = null;
        }
    }

    @FXML
    private void handleUpdateQuestion() {
        // Valider les champs
        if (questionTextArea.getText().isEmpty()) {
            showAlert("Erreur", "Le texte de la question ne peut pas être vide.");
            return;
        }
        if (option1Field.getText().isEmpty() || option2Field.getText().isEmpty() ||
                option3Field.getText().isEmpty() || option4Field.getText().isEmpty()) {
            showAlert("Erreur", "Toutes les options doivent être remplies.");
            return;
        }

        // Vérifier qu'au moins une réponse correcte est sélectionnée
        List<String> correctAnswers = new ArrayList<>();
        if (correctOption1.isSelected()) correctAnswers.add(option1Field.getText());
        if (correctOption2.isSelected()) correctAnswers.add(option2Field.getText());
        if (correctOption3.isSelected()) correctAnswers.add(option3Field.getText());
        if (correctOption4.isSelected()) correctAnswers.add(option4Field.getText());

        if (correctAnswers.isEmpty()) {
            showAlert("Erreur", "Vous devez sélectionner au moins une réponse correcte.");
            return;
        }

        // Mettre à jour la question
        questionToUpdate.setText(questionTextArea.getText());
        questionToUpdate.setOptionType(radioType.isSelected() ? "radio" : "checkbox");
        questionToUpdate.setOption1(option1Field.getText());
        questionToUpdate.setOption2(option2Field.getText());
        questionToUpdate.setOption3(option3Field.getText());
        questionToUpdate.setOption4(option4Field.getText());

        // Sauvegarder les réponses correctes
        questionToUpdate.setCorrectAnswers("[" + String.join(",", correctAnswers.stream().map(s -> "\"" + s + "\"").toList()) + "]");

        try {
            questionService.update(questionToUpdate);
            showAlert("Succès", "Question mise à jour avec succès (Backend) !");
            goBackToQuestionList();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour de la question : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        goBackToQuestionList();
    }

    private void goBackToQuestionList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherQuestionsBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /afficherQuestionsBack.fxml");
            }
            Parent root = loader.load();

            affichageQuestionBack controller = loader.getController();
            controller.setQuizId(quizId);

            Stage stage = (Stage) updateQuestionButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Questions du Quiz (Backend)");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du retour à la liste des questions : " + e.toString());
        }
    }

    private List<String> parseCorrectAnswers(String correctAnswers) {
        List<String> answers = new ArrayList<>();
        if (correctAnswers == null || correctAnswers.equals("[]")) {
            return answers;
        }
        // Supprimer les crochets et guillemets pour parser les réponses
        String cleaned = correctAnswers.replace("[", "").replace("]", "").replace("\"", "");
        if (!cleaned.isEmpty()) {
            for (String answer : cleaned.split(",")) {
                answers.add(answer.trim());
            }
        }
        return answers;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}