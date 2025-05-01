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
import util.DataSource;
import entite.Session;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;

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

            // Vérifier la session active et récupérer l'ID de l'utilisateur connecté
            Session session = Session.getInstance();
            if (!session.isActive()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Aucune session active. Veuillez vous connecter.");
                alert.showAndWait();
                return;
            }
            int userId = session.getUserId();

            // Charger l'utilisateur depuis la base de données avec l'ID récupéré
            User user = userService.getUserById(userId);
            if (user == null) {
                throw new IllegalStateException("Utilisateur avec ID " + userId + " introuvable");
            }

            QuizResult quizResult = new QuizResult(quiz, user, noteToStore);
            quizResultService.addQuizResult(quizResult);
            System.out.println("Résultat du quiz enregistré dans quiz_result pour quiz_id=" + quizId + ", user_id=" + userId + ", note=" + noteToStore);

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

            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1500);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 2. Load body (affichageEtudiantQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageEtudiantQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/affichageEtudiantQuiz.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // 3. Load footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1500);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap the VBox in a ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Calculate required height
            double totalHeight = headerImageView.getFitHeight() +
                    bodyContent.prefHeight(-1) +
                    footerImageView.getFitHeight();

            // Set scene to specified size
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }

            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) {
                scene.getStylesheets().add(navBarCss.toExternalForm());
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