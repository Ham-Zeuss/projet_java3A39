package Controller.Oumaima;

import entite.Oumaima.Question;
import entite.Oumaima.Quiz;
import entite.User;
import entite.Oumaima.QuizResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.Oumaima.QuestionService;
import service.Oumaima.QuizService;
import service.Oumaima.QuizResultService;
import service.UserService;
import util.DataSource; // Importer DataSource

import java.net.URL;
import java.sql.Connection;

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
    private QuizResultService quizResultService;
    private UserService userService;
    private int quizId;
    private List<Question> questions;
    private final Map<Question, List<CheckBox>> questionCheckBoxes = new HashMap<>();
    private final Map<Question, List<RadioButton>> questionRadioButtons = new HashMap<>();

    // Constructeur utilisant DataSource pour la connexion
    public AfficherQuestionsEtudiantController() {
        Connection connection = DataSource.getInstance().getConnection();
        if (connection == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur : Connexion à la base de données nulle");
            alert.showAndWait();
            throw new IllegalStateException("Impossible d'établir une connexion à la base de données");
        }
        this.quizResultService = new QuizResultService(connection);
        this.userService = new UserService();
    }

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

        Label questionText = new Label(question.getText());
        questionText.getStyleClass().add("question-text");

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

        box.getChildren().add(0, questionText);
        return box;
    }

    @FXML
    private void submitAllAnswers() {
        int correctAnswersCount = 0;
        int totalQuestions = questions.size();
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
            String submittedAnswer = null;

            if ("checkbox".equals(optionType)) {
                List<CheckBox> checkBoxes = questionCheckBoxes.get(question);
                if (checkBoxes != null) {
                    List<String> selectedOptions = new ArrayList<>();
                    for (int i = 0; i < checkBoxes.size(); i++) {
                        if (checkBoxes.get(i).isSelected()) {
                            selectedOptions.add(options[i]);
                        }
                    }
                    if (selectedOptions.isEmpty()) {
                        submittedAnswer = null;
                        reponseSoumise.append("]");
                    } else {
                        Collections.sort(selectedOptions);
                        String jsonArray = "[\"" + String.join("\",\"", selectedOptions) + "\"]";
                        submittedAnswer = jsonArray;
                        reponseSoumise.append(String.join(",", selectedOptions)).append("]");
                    }
                } else {
                    reponseSoumise.append("]");
                }
            } else if ("radio".equals(optionType) || "radiobox".equals(optionType)) {
                List<RadioButton> radioButtons = questionRadioButtons.get(question);
                if (radioButtons != null) {
                    for (int i = 0; i < radioButtons.size(); i++) {
                        if (radioButtons.get(i).isSelected()) {
                            submittedAnswer = "[\"" + options[i] + "\"]";
                            reponseSoumise.append(options[i]).append("]");
                            break;
                        }
                    }
                } else {
                    reponseSoumise.append("]");
                }
            } else {
                reponseSoumise.append("]");
            }

            question.setReponseSoumise(submittedAnswer);

            System.out.println("Tentative de mise à jour de la question ID=" + question.getId());
            System.out.println("Données de la question : " + question.toString());

            try {
                questionService.update(question);
                System.out.println("Réponse soumise enregistrée pour la question " + question.getId() + " : " + submittedAnswer);
            } catch (Exception e) {
                e.printStackTrace();
                String errorMessage = "Erreur lors de l'enregistrement de la réponse soumise pour la question ID=" + question.getId() + " : " + e.getMessage() + "\nStackTrace: " + e.toString();
                System.err.println(errorMessage);
                Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
                alert.showAndWait();
            }

            String normalizedCorrectAnswers = normalizeAnswer(correctAnswers);
            String normalizedSubmittedAnswer = normalizeAnswer(submittedAnswer != null ? submittedAnswer : "[]");

            System.out.println("Debug Correct Answers: " + toDebugString(correctAnswers));
            System.out.println("Debug Normalized Correct: " + toDebugString(normalizedCorrectAnswers));
            System.out.println("Debug Submitted Answer: " + toDebugString(submittedAnswer != null ? submittedAnswer : "[]"));
            System.out.println("Debug Normalized Submitted: " + toDebugString(normalizedSubmittedAnswer));

            isCorrect = normalizedSubmittedAnswer.equals(normalizedCorrectAnswers);
            if (!isCorrect) {
                System.out.println("Comparison failed. Lengths: Correct=" + normalizedCorrectAnswers.length() +
                        ", Submitted=" + normalizedSubmittedAnswer.length());
                System.out.println("Are strings equal? " + normalizedSubmittedAnswer.equals(normalizedCorrectAnswers));
            }

            System.out.println("Question: " + question.getText());
            System.out.println("Raw Correct Answers: " + correctAnswers);
            System.out.println("Normalized Correct Answers: " + normalizedCorrectAnswers);
            System.out.println("Submitted Answer: " + (submittedAnswer != null ? submittedAnswer : "[]"));
            System.out.println("Normalized Submitted Answer: " + normalizedSubmittedAnswer);
            System.out.println("Is Correct: " + isCorrect);
            System.out.println("Options: A) " + options[0] + ", B) " + options[1] + ", C) " + options[2] + ", D) " + options[3]);
            System.out.println("---");

            if (isCorrect) {
                correctAnswersCount++;
            }

            result.append("Question: ").append(question.getText()).append("\n");
            result.append("Réponse soumise: ").append(reponseSoumise).append("\n");
            result.append("Réponse correcte: ").append(correctAnswers).append("\n\n");
        }

        String finalNoteDisplay = correctAnswersCount + "/" + totalQuestions;
        result.append("Note finale: ").append(finalNoteDisplay);

        float noteToStore = correctAnswersCount;
        try {
            Quiz quiz = quizService.readById(quizId);
            if (quiz == null) {
                throw new IllegalStateException("Quiz avec ID " + quizId + " introuvable");
            }
            quiz.setNote(noteToStore);
            quizService.update(quiz);
            System.out.println("Note enregistrée pour le quiz " + quizId + " : " + noteToStore);

            // Charger l'utilisateur depuis la base de données
            User user = userService.getUserById(22);
            if (user == null) {
                throw new IllegalStateException("Utilisateur avec ID 22 introuvable");
            }

            QuizResult quizResult = new QuizResult(quiz, user, noteToStore);
            quizResultService.addQuizResult(quizResult);
            System.out.println("Résultat du quiz enregistré dans quiz_result pour quiz_id=" + quizId + ", user_id=22, note=" + noteToStore);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement de la note ou du résultat : " + e.getMessage() + "\nClasse de l'erreur : " + e.getClass().getName());
            alert.showAndWait();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, result.toString());
        alert.setTitle("Résultat du Quiz");
        alert.showAndWait();
    }

    private String normalizeAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return "[]";
        }
        String cleaned = answer.replaceAll("[\"\\s]+", "").replaceAll("\\\\\"", "");
        String normalized = cleaned.toLowerCase();
        if (!normalized.startsWith("[") || !normalized.endsWith("]")) {
            normalized = "[" + normalized.replaceAll("[\\[\\]]", "") + "]";
        }
        if (normalized.length() > 3 && normalized.contains(",")) {
            String content = normalized.substring(1, normalized.length() - 1);
            String[] options = content.split(",");
            List<String> optionList = new ArrayList<>();
            for (String opt : options) {
                String trimmed = opt.trim();
                if (!trimmed.isEmpty()) {
                    optionList.add(trimmed);
                }
            }
            Collections.sort(optionList);
            normalized = "[" + String.join(",", optionList) + "]";
        }
        return normalized;
    }

    private String toDebugString(String str) {
        StringBuilder debug = new StringBuilder();
        debug.append("Length=").append(str.length()).append(", Chars=[");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            String hex = Integer.toHexString(c);
            debug.append("0x").append(hex).append("(").append(c).append(")");
            if (i < str.length() - 1) {
                debug.append(",");
            }
        }
        debug.append("]");
        return debug.toString();
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

            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageEtudiantQuiz.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

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