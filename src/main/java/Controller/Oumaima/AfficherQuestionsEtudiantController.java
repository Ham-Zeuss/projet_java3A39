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
import javafx.scene.layout.HBox;
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
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

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
        VBox box = new VBox(8); // Reduced spacing for tighter layout
        box.getStyleClass().add("question-card");

        Label questionText = new Label(question.getText());
        questionText.getStyleClass().add("question-text");

        VBox optionsContainer = new VBox(6); // Container for all options
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
            int optionIndex = 1;
            for (String option : options) {
                HBox optionRow = new HBox(6);
                optionRow.setAlignment(Pos.CENTER_LEFT);
                VBox checkBoxCard = new VBox(4);
                checkBoxCard.getStyleClass().add("custom-checkbox");
                CheckBox checkBox = new CheckBox();
                checkBoxes.add(checkBox);
                checkBoxCard.getChildren().add(checkBox);
                Label optionLabel = new Label(option);
                optionLabel.getStyleClass().add("option-label");
                optionLabel.getStyleClass().add("option-" + optionIndex); // Add unique class
                optionRow.getChildren().addAll(checkBoxCard, optionLabel);
                optionsContainer.getChildren().add(optionRow);
                optionIndex++;
            }
            questionCheckBoxes.put(question, checkBoxes);
        } else if ("radio".equals(optionType) || "radiobox".equals(optionType)) {
            int optionIndex = 1;
            for (String option : options) {
                HBox optionRow = new HBox(6);
                optionRow.setAlignment(Pos.CENTER_LEFT);
                VBox radioBoxCard = new VBox(4);
                radioBoxCard.getStyleClass().add("custom-radiobox");
                RadioButton radioButton = new RadioButton();
                radioButton.setToggleGroup(toggleGroup);
                radioButtons.add(radioButton);
                radioBoxCard.getChildren().add(radioButton);
                Label optionLabel = new Label(option);
                optionLabel.getStyleClass().add("option-label");
                optionLabel.getStyleClass().add("option-" + optionIndex); // Add unique class
                optionRow.getChildren().addAll(radioBoxCard, optionLabel);
                optionsContainer.getChildren().add(optionRow);
                optionIndex++;
            }
            questionRadioButtons.put(question, radioButtons);
        } else {
            Label errorLabel = new Label("Type d'option inconnu : " + optionType);
            errorLabel.setStyle("-fx-text-fill: red;");
            optionsContainer.getChildren().add(errorLabel);
        }

        box.getChildren().addAll(questionText, optionsContainer);
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

            try {
                questionService.update(question);
            } catch (Exception e) {
                e.printStackTrace();
                String errorMessage = "Erreur lors de l'enregistrement de la réponse soumise pour la question ID=" + question.getId() + " : " + e.getMessage();
                Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
                alert.showAndWait();
            }

            String normalizedCorrectAnswers = normalizeAnswer(correctAnswers);
            String normalizedSubmittedAnswer = normalizeAnswer(submittedAnswer != null ? submittedAnswer : "[]");

            isCorrect = normalizedSubmittedAnswer.equals(normalizedCorrectAnswers);
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

            Session session = Session.getInstance();
            if (!session.isActive()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Aucune session active. Veuillez vous connecter.");
                alert.showAndWait();
                return;
            }
            int userId = session.getUserId();
            User user = userService.getUserById(userId);
            if (user == null) {
                throw new IllegalStateException("Utilisateur avec ID " + userId + " introuvable");
            }

            QuizResult quizResult = new QuizResult(quiz, user, noteToStore);
            quizResultService.addQuizResult(quizResult);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement de la note ou du résultat : " + e.getMessage());
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
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            Stage stage = (Stage) backButton.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load navbar (header.fxml)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(width * 0.6, 100);
            VBox.setMargin(headerFxmlContent, new Insets(0, 0, 10, 0));
            mainContent.getChildren().add(headerFxmlContent);

            // Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(width);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load body content
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageEtudiantQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/affichageEtudiantQuiz.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Add footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(width);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
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
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Create scene
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) scene.getStylesheets().add(navBarCss.toExternalForm());
            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());
            URL appointments = getClass().getResource("/css/appointments.css");
            if (appointments != null) scene.getStylesheets().add(appointments.toExternalForm());
            URL gooButton = getClass().getResource("/css/GooButton.css");
            if (gooButton != null) scene.getStylesheets().add(gooButton.toExternalForm());
            URL gamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStyling != null) scene.getStylesheets().add(gamesMenuStyling.toExternalForm());
            URL profileCard = getClass().getResource("/css/profile-card.css");
            if (profileCard != null) scene.getStylesheets().add(profileCard.toExternalForm());
            URL designFull = getClass().getResource("/DesignFull.css");
            if (designFull != null) scene.getStylesheets().add(designFull.toExternalForm());
            URL commentsStyle = getClass().getResource("/css/CommentsStyle.css");
            if (commentsStyle != null) scene.getStylesheets().add(commentsStyle.toExternalForm());
            URL oumaimaStyle = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (oumaimaStyle != null) scene.getStylesheets().add(oumaimaStyle.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Liste des Quiz");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }
}