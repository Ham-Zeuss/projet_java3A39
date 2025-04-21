package Controller.Oumaima;

import com.google.gson.Gson;
import entite.Oumaima.Question;
import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import service.Oumaima.QuestionService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class addQuestionBackController implements Initializable {

    private int quizId;

    @FXML
    private TextArea questionTextArea;

    @FXML
    private TextField option1Field, option2Field, option3Field, option4Field;

    @FXML
    private CheckBox correctOption1, correctOption2, correctOption3, correctOption4;

    @FXML
    private RadioButton radioType, checkboxType;

    @FXML
    private Button addQuestionButton;

    private final QuestionService questionService = new QuestionService();

    public void initData(int quizId) {
        this.quizId = quizId;
        System.out.println("ID du quiz reçu (Backend) : " + quizId);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Écoute des clics sur les types
        radioType.setOnAction(e -> setupRadioMode());
        checkboxType.setOnAction(e -> setupCheckboxMode());

        // Mode par défaut : checkbox multiple
        setupCheckboxMode();
    }

    private void setupRadioMode() {
        setupExclusiveCheckbox(correctOption1, correctOption2, correctOption3, correctOption4);
    }

    private void setupCheckboxMode() {
        // Supprime tous les anciens comportements (désactive l'exclusivité)
        correctOption1.setOnAction(null);
        correctOption2.setOnAction(null);
        correctOption3.setOnAction(null);
        correctOption4.setOnAction(null);
    }

    private void setupExclusiveCheckbox(CheckBox... checkBoxes) {
        for (CheckBox cb : checkBoxes) {
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    for (CheckBox other : checkBoxes) {
                        if (other != cb) {
                            other.setSelected(false);
                        }
                    }
                }
            });
        }
    }

    @FXML
    private void handleAddQuestion(ActionEvent event) {
        String texte = questionTextArea.getText().trim();
        String opt1 = option1Field.getText().trim();
        String opt2 = option2Field.getText().trim();
        String opt3 = option3Field.getText().trim();
        String opt4 = option4Field.getText().trim();

        if (texte.isEmpty() || opt1.isEmpty() || opt2.isEmpty() || opt3.isEmpty() || opt4.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs manquants", "Veuillez remplir toutes les options.");
            return;
        }

        // Vérifier le type de question sélectionné
        String optionType;
        if (radioType.isSelected()) {
            optionType = "radio";
        } else if (checkboxType.isSelected()) {
            optionType = "checkbox";
        } else {
            showAlert(Alert.AlertType.ERROR, "Type de question manquant", "Veuillez sélectionner un type de question.");
            return;
        }

        // Récolter les réponses correctes
        List<String> correctAnswers = new ArrayList<>();
        if (correctOption1.isSelected()) correctAnswers.add(opt1);
        if (correctOption2.isSelected()) correctAnswers.add(opt2);
        if (correctOption3.isSelected()) correctAnswers.add(opt3);
        if (correctOption4.isSelected()) correctAnswers.add(opt4);

        if (correctAnswers.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Réponse manquante", "Veuillez sélectionner au moins une réponse correcte.");
            return;
        }

        // Valider que si radio, une seule réponse correcte est sélectionnée
        if (optionType.equals("radio") && correctAnswers.size() > 1) {
            showAlert(Alert.AlertType.ERROR, "Trop de réponses", "Une seule réponse correcte est autorisée pour le type Radio.");
            return;
        }

        Question question = new Question();
        question.setQuiz(new Quiz(quizId));
        question.setText(texte);
        question.setOption1(opt1);
        question.setOption2(opt2);
        question.setOption3(opt3);
        question.setOption4(opt4);
        question.setCorrectAnswers(new Gson().toJson(correctAnswers)); // JSON
        question.setOptionType(optionType);

        try {
            questionService.create(question);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Question ajoutée avec succès (Backend) !");
            clearForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    private void clearForm() {
        questionTextArea.clear();
        option1Field.clear();
        option2Field.clear();
        option3Field.clear();
        option4Field.clear();
        correctOption1.setSelected(false);
        correctOption2.setSelected(false);
        correctOption3.setSelected(false);
        correctOption4.setSelected(false);
        radioType.setSelected(false);
        checkboxType.setSelected(true); // Par défaut on revient à "checkbox"
        setupCheckboxMode();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}