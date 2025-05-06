package Controller.Oumaima;

import entite.Oumaima.Question;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import service.Oumaima.QuestionService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;

public class UpdateQuestionController implements Initializable {

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set "Enregistrer" text and action for updateQuestionButton
        if (updateQuestionButton != null) {
            updateQuestionButton.setText("Enregistrer");
            updateQuestionButton.setOnAction(e -> handleUpdateQuestion());
        }

        // Set "Retour" text and action for cancelButton
        if (cancelButton != null) {
            cancelButton.setText("Retour");
            cancelButton.setOnAction(e -> handleCancel());
        }
    }

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
            showAlert("Succès", "Question mise à jour avec succès !");
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
            Stage stage = (Stage) updateQuestionButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestions.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/afficherQuestions.fxml introuvable");
            }
            Parent bodyContent = loader.load();
            AfficherQuestionsController controller = loader.getController();
            controller.displayQuestionsPage(stage, quizId);
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

    public void displayUpdateQuestionPage(Stage stage, Question question, int quizId) {
        try {
            setQuestionToUpdate(question, quizId); // Set the question and quizId
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Add header image as the first element
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

            // 2. Load body (updateQuestion.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuestion.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/updateQuestion.fxml introuvable");
            }
            bodyLoader.setController(this); // Set this controller to handle the FXML
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // 3. Load footer as ImageView
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

            URL NavBar = getClass().getResource("/navbar.css");
            if (NavBar != null) {
                scene.getStylesheets().add(NavBar.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Modifier une Question");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page de modification de question : " + e.getMessage());
        }
    }
}