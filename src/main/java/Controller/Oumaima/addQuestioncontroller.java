package Controller.Oumaima;

import com.google.gson.Gson;
import entite.Oumaima.Question;
import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
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

public class addQuestioncontroller implements Initializable {

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

    @FXML
    private Button backButton;

    private final QuestionService questionService = new QuestionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Écoute des clics sur les types
        radioType.setOnAction(e -> setupRadioMode());
        checkboxType.setOnAction(e -> setupCheckboxMode());

        // Mode par défaut : checkbox multiple
        setupCheckboxMode();

        // Set "Enregistrer" text and action for addQuestionButton
        if (addQuestionButton != null) {
            addQuestionButton.setText("Enregistrer");
            addQuestionButton.setOnAction(this::handleAddQuestion);
        }

        // Set "Retour" text and action for backButton
        if (backButton != null) {
            backButton.setText("Retour");
            backButton.setOnAction(e -> goBackToQuizList());
        }
    }

    public void initData(int quizId) {
        this.quizId = quizId;
        System.out.println("ID du quiz reçu : " + quizId);
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
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Question ajoutée avec succès !");
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

    @FXML
    private void goBackToQuizList() {
        try {
            // Vérifie si backButton est null
            if (backButton == null) {
                throw new IllegalStateException("backButton is null");
            }

            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();
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

            // 2. Load body (affichageQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/affichageQuiz.fxml introuvable");
            }
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
            stage.setTitle("Liste des Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la liste des quiz : " + e.getMessage());
        }
    }

    public void displayAddQuestionPage(Stage stage, int quizId) {
        try {
            initData(quizId); // Set the quizId
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

            // 2. Load body (addQuestion.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuestion.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/addQuestion.fxml introuvable");
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
            stage.setTitle("Ajouter une Question");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page d'ajout de question : " + e.getMessage());
        }
    }
}