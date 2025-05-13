package Controller.Oumaima;

import entite.Oumaima.Question;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import service.Oumaima.QuestionService;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.stage.Screen;

public class AfficherQuestionsController implements Initializable {

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure buttons with icons and text
        if (backButton != null) {
            setupButton(backButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Retour", true);
            backButton.setOnAction(e -> goBackToQuizList());
        }

        if (submitAllButton != null) {
            setupButton(submitAllButton, "https://img.icons8.com/?size=100&id=94194&format=png&color=000000", "Enregistrer", true);
            submitAllButton.setOnAction(e -> submitAllAnswers());
        }
    }

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
        Button updateButton = new Button();
        Button deleteButton = new Button();

        // Configure buttons with icons and text
        setupButton(updateButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Modifier", true);
        setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Supprimer", true);

        updateButton.setOnAction(e -> goToUpdateQuestion(question));

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

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            // Show text only if showText is true
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60); // Larger width for buttons with text
            // Apply specified style
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            // Apply same style in fallback case
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        }
    }

    private void goToUpdateQuestion(Question question) {
        try {
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            Stage stage = (Stage) questionContainer.getScene().getWindow();
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
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuestion.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/updateQuestion.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Set controller data
            UpdateQuestionController controller = bodyLoader.getController();
            controller.setQuestionToUpdate(question, quizId);

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
            URL navBar = getClass().getResource("/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
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
            stage.setTitle("Modifier une Question");
            stage.setResizable(true);
            stage.centerOnScreen();
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

            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            // Get the current stage
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
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/affichageQuiz.fxml introuvable");
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
            URL navBar = getClass().getResource("/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des quiz : " + e.toString());
            alert.showAndWait();
        }
    }

    public void displayQuestionsPage(Stage stage, int quizId) {
        try {
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

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
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestions.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/afficherQuestions.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Retrieve the controller and set quizId
            AfficherQuestionsController controller = bodyLoader.getController();
            controller.setQuizId(quizId);

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
            URL navBar = getClass().getResource("/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
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
            stage.setTitle("Questions du Quiz");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la page des questions : " + e.getMessage());
            alert.showAndWait();
        }
    }
}