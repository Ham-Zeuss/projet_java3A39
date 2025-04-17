package controller;

import entite.Question;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.QuestionService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfficherQuestionsController {

    @FXML
    private VBox questionContainer;

    @FXML
    private Button backButton;

    @FXML
    private Button submitAllButton;

    private final QuestionService questionService = new QuestionService();
    private int quizId;
    private List<Question> questions;
    private final Map<Question, List<CheckBox>> questionCheckBoxes = new HashMap<>();
    private final Map<Question, List<RadioButton>> questionRadioButtons = new HashMap<>();

    public void setQuizId(int quizId) {
        this.quizId = quizId;
        afficherQuestions();
    }

    private void afficherQuestions() {
        questionContainer.getChildren().clear();
        questionCheckBoxes.clear();
        questionRadioButtons.clear();

        try {
            questions = questionService.getQuestionsByQuizId(quizId);
            if (questions.isEmpty()) {
                Label noQuestions = new Label("Aucune question trouvée pour ce quiz.");
                noQuestions.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
                questionContainer.getChildren().add(noQuestions);
                return;
            }

            for (Question question : questions) {
                System.out.println("Type de question pour " + question.getText() + " : " + question.getOptionType());
                VBox box = createQuestionBox(question);
                questionContainer.getChildren().add(box);
            }
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des questions : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private VBox createQuestionBox(Question question) {
        VBox box = new VBox(10);
        box.getStyleClass().add("question-card");

        // 1. Texte de la question
        Label questionText = new Label(question.getText());
        questionText.getStyleClass().add("question-text");

        // 2. Options (A, B, C, D)
        List<CheckBox> checkBoxes = new ArrayList<>();
        List<RadioButton> radioButtons = new ArrayList<>();
        ToggleGroup toggleGroup = new ToggleGroup();

        String[] options = {
                safeText(question.getOption1()),
                safeText(question.getOption2()),
                safeText(question.getOption3()),
                safeText(question.getOption4())
        };

        String optionType = question.getOptionType() != null ? question.getOptionType().toLowerCase() : "";
        if ("checkbox".equals(optionType)) {
            for (int i = 0; i < options.length; i++) {
                CheckBox checkBox = new CheckBox((char) ('A' + i) + ") " + options[i]);
                checkBox.getStyleClass().add("check-box");
                checkBoxes.add(checkBox);
                box.getChildren().add(checkBox);
            }
            questionCheckBoxes.put(question, checkBoxes);
        } else if ("radio".equals(optionType) || "radiobox".equals(optionType)) {
            for (int i = 0; i < options.length; i++) {
                RadioButton radioButton = new RadioButton((char) ('A' + i) + ") " + options[i]);
                radioButton.setToggleGroup(toggleGroup);
                radioButton.getStyleClass().add("radio-button");
                radioButtons.add(radioButton);
                box.getChildren().add(radioButton);
            }
            questionRadioButtons.put(question, radioButtons);
        } else {
            Label errorLabel = new Label("Type d'option inconnu : " + optionType);
            errorLabel.setStyle("-fx-text-fill: red;");
            box.getChildren().add(errorLabel);
        }

        // 3. Boutons "Modifier" et "Supprimer"
        Button updateButton = new Button("Modifier");
        updateButton.getStyleClass().add("update-button");
        updateButton.setOnAction(e -> goToUpdateQuestion(question));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> {
            questionService.delete(question);
            questionCheckBoxes.remove(question);
            questionRadioButtons.remove(question);
            questions.remove(question);
            afficherQuestions();
        });

        HBox buttonContainer = new HBox(10, updateButton, deleteButton);
        buttonContainer.setStyle("-fx-alignment: center;");

        // Ajouter les éléments dans l'ordre : texte, options, boutons
        box.getChildren().add(0, questionText);
        box.getChildren().add(buttonContainer);

        return box;
    }

    private void goToUpdateQuestion(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateQuestion.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /updateQuestion.fxml");
            }
            Parent root = loader.load();

            UpdateQuestionController controller = loader.getController();
            controller.setQuestionToUpdate(question, quizId);

            Stage stage = (Stage) questionContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Modifier une Question");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la modification : " + e.toString());
            alert.showAndWait();
        }
    }

    @FXML
    private void submitAllAnswers() {
        StringBuilder result = new StringBuilder("Réponses soumises :\n");
        boolean hasAnswers = false;

        for (Question question : questions) {
            StringBuilder reponseSoumise = new StringBuilder("[");
            String optionType = question.getOptionType() != null ? question.getOptionType().toLowerCase() : "";

            String[] options = {
                    safeText(question.getOption1()),
                    safeText(question.getOption2()),
                    safeText(question.getOption3()),
                    safeText(question.getOption4())
            };

            if ("checkbox".equals(optionType)) {
                List<CheckBox> checkBoxes = questionCheckBoxes.get(question);
                if (checkBoxes != null) {
                    List<String> selectedOptions = new ArrayList<>();
                    for (int i = 0; i < checkBoxes.size(); i++) {
                        if (checkBoxes.get(i).isSelected()) {
                            selectedOptions.add("\"" + options[i] + "\"");
                        }
                    }
                    if (!selectedOptions.isEmpty()) {
                        reponseSoumise.append(String.join(",", selectedOptions));
                    }
                }
            } else if ("radio".equals(optionType) || "radiobox".equals(optionType)) {
                List<RadioButton> radioButtons = questionRadioButtons.get(question);
                if (radioButtons != null) {
                    for (int i = 0; i < radioButtons.size(); i++) {
                        if (radioButtons.get(i).isSelected()) {
                            reponseSoumise.append("\"").append(options[i]).append("\"");
                            break;
                        }
                    }
                }
            }
            reponseSoumise.append("]");
            question.setReponseSoumise(reponseSoumise.toString());
            questionService.update(question);

            if (!reponseSoumise.toString().equals("[]")) {
                result.append("Question: ").append(question.getText()).append("\nRéponse: ").append(reponseSoumise).append("\n");
                hasAnswers = true;
            }
        }

        if (hasAnswers) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, result.toString());
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Aucune réponse sélectionnée.");
            alert.showAndWait();
        }
    }

    private String safeText(String text) {
        return text != null && !text.trim().isEmpty() ? text : "(option vide)";
    }

    @FXML
    private void goBackToQuizList() {
        try {
            // Vérifie si backButton est null
            if (backButton == null) {
                throw new IllegalStateException("backButton est null");
            }

            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();
            VBox mainContent = new VBox();

            // Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load header.html
            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            // Load body (affichageQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/affichageQuiz.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            // Load footer.html
            WebView footerWebView = new WebView();
            URL footerUrl = getClass().getResource("/footer.html");
            if (footerUrl != null) {
                footerWebView.getEngine().load(footerUrl.toExternalForm());
            } else {
                footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
            }
            footerWebView.setPrefSize(1000, 830);
            mainContent.getChildren().add(footerWebView);

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            Scene scene = new Scene(scrollPane, 600, 400);
            URL cssUrl = getClass().getResource("/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
            if (userTitlesCssUrl != null) {
                scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Liste des Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des quiz : " + e.toString());
            alert.showAndWait();
        }
    }
}