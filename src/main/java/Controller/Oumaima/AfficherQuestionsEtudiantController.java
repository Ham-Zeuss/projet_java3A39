package Controller.Oumaima;

import entite.Oumaima.Question;
import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.Oumaima.QuestionService;
import service.Oumaima.QuizService;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfficherQuestionsEtudiantController {

    @FXML
    private VBox questionContainer;

    @FXML
    private Button backButton;

    @FXML
    private Button submitAllButton;

    private final QuestionService questionService = new QuestionService();
    private final QuizService quizService = new QuizService();
    private int quizId;
    private List<Question> questions;
    private final Map<Question, List<CheckBox>> questionCheckBoxes = new HashMap<>();
    private final Map<Question, List<RadioButton>> questionRadioButtons = new HashMap<>();

    public void setQuizId(int quizId) {
        if (quizId <= 0) {
            throw new IllegalArgumentException("L'ID du quiz doit être supérieur à 0");
        }
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
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des questions : " + e.getMessage() + "\n" + e.getClass().getName());
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

        // Ajouter le texte de la question
        box.getChildren().add(0, questionText);

        return box;
    }

    @FXML
    private void submitAllAnswers() {
        int correctAnswersCount = 0; // Correspond à 'a' (nombre de réponses correctes)
        int totalQuestions = questions.size(); // Correspond à 'b' (nombre total de questions)
        StringBuilder result = new StringBuilder("Résultat du Quiz :\n");

        for (Question question : questions) {
            StringBuilder reponseSoumise = new StringBuilder("[");
            String optionType = question.getOptionType() != null ? question.getOptionType().toLowerCase() : "";
            String correctAnswers = question.getCorrectAnswers() != null ? question.getCorrectAnswers().trim() : "[]";

            String[] options = {
                    safeText(question.getOption1()),
                    safeText(question.getOption2()),
                    safeText(question.getOption3()),
                    safeText(question.getOption4())
            };

            boolean isCorrect = false;
            String submittedAnswer = "[]"; // Par défaut, si aucune réponse n'est sélectionnée

            if ("checkbox".equals(optionType)) {
                List<CheckBox> checkBoxes = questionCheckBoxes.get(question);
                if (checkBoxes != null) {
                    List<String> selectedOptions = new ArrayList<>();
                    for (int i = 0; i < checkBoxes.size(); i++) {
                        if (checkBoxes.get(i).isSelected()) {
                            selectedOptions.add(options[i]);
                        }
                    }
                    // Trier les options pour ignorer l'ordre
                    Collections.sort(selectedOptions);
                    submittedAnswer = selectedOptions.isEmpty() ? "[]" : "[" + String.join(",", selectedOptions) + "]";
                    reponseSoumise.append(String.join(",", selectedOptions));
                }
            } else if ("radio".equals(optionType) || "radiobox".equals(optionType)) {
                List<RadioButton> radioButtons = questionRadioButtons.get(question);
                if (radioButtons != null) {
                    boolean hasSelection = false;
                    for (int i = 0; i < radioButtons.size(); i++) {
                        if (radioButtons.get(i).isSelected()) {
                            String selectedOption = options[i];
                            submittedAnswer = "[" + selectedOption + "]";
                            reponseSoumise.append(selectedOption);
                            hasSelection = true;
                            break;
                        }
                    }
                    if (!hasSelection) {
                        reponseSoumise.append("Aucune réponse sélectionnée");
                    }
                }
            }
            reponseSoumise.append("]");

            // Normalisation de correctAnswers pour la comparaison
            String normalizedCorrectAnswers = correctAnswers;
            // Si correctAnswers n'a pas de crochets, en ajouter
            if (!correctAnswers.startsWith("[") && !correctAnswers.endsWith("]")) {
                normalizedCorrectAnswers = "[" + correctAnswers + "]";
            }
            // Normalisation supplémentaire : retirer les espaces, passer en minuscules
            normalizedCorrectAnswers = normalizedCorrectAnswers.replaceAll("\\s+", "").toLowerCase();
            submittedAnswer = submittedAnswer.replaceAll("\\s+", "").toLowerCase();

            // Pour les checkbox, trier les options dans correctAnswers
            if ("checkbox".equals(optionType) && normalizedCorrectAnswers.length() > 2) {
                String[] correctOptions = normalizedCorrectAnswers.substring(1, normalizedCorrectAnswers.length() - 1).split(",");
                List<String> correctOptionsList = new ArrayList<>(List.of(correctOptions));
                Collections.sort(correctOptionsList);
                normalizedCorrectAnswers = "[" + String.join(",", correctOptionsList) + "]";
            }

            // Comparaison
            isCorrect = submittedAnswer.equals(normalizedCorrectAnswers);

            // Logs détaillés pour déboguer
            System.out.println("Question: " + question.getText());
            System.out.println("Raw Correct Answers: " + correctAnswers);
            System.out.println("Normalized Correct Answers: " + normalizedCorrectAnswers);
            System.out.println("Submitted Answer: " + submittedAnswer);
            System.out.println("Is Correct: " + isCorrect);
            System.out.println("---");

            if (isCorrect) {
                correctAnswersCount++; // Incrémenter 'a' si la réponse est correcte
            }

            result.append("Question: ").append(question.getText()).append("\n");
            result.append("Réponse soumise: ").append(reponseSoumise).append("\n");
            result.append("Réponse correcte: ").append(correctAnswers).append("\n\n");
        }

        // Format de la note : a/b pour l'affichage
        String finalNoteDisplay = correctAnswersCount + "/" + totalQuestions;
        result.append("Note finale: ").append(finalNoteDisplay);

        // Calculer le pourcentage pour l'enregistrement dans la table quiz (colonne note de type FLOAT)
        float notePercentage = totalQuestions > 0 ? (float) correctAnswersCount / totalQuestions * 100 : 0;

        // Enregistrer la note dans la table quiz
        try {
            Quiz quiz = quizService.readById(quizId);
            if (quiz == null) {
                throw new IllegalStateException("Quiz avec ID " + quizId + " introuvable");
            }
            quiz.setNote(notePercentage);
            quizService.update(quiz);
            System.out.println("Note enregistrée pour le quiz " + quizId + " : " + notePercentage + "%");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement de la note : " + e.getMessage());
            alert.showAndWait();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, result.toString());
        alert.setTitle("Résultat du Quiz");
        alert.showAndWait();
    }

    private String safeText(String text) {
        return text != null && !text.trim().isEmpty() ? text : "(option vide)";
    }

    @FXML
    private void goBackToQuizList() {
        try {
            if (backButton == null) {
                throw new IllegalStateException("backButton est null");
            }

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

            // Load body (affichageEtudiantQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageEtudiantQuiz.fxml"));
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
            URL cssUrl = getClass().getResource("/OumaimaFXML/styles.css");
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des quiz : " + e.getMessage() + "\n" + e.getClass().getName());
            alert.showAndWait();
        }
    }
}