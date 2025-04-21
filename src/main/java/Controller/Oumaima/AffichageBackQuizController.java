package Controller.Oumaima;

import entite.Oumaima.Quiz;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.Oumaima.QuizService;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class AffichageBackQuizController {

    @FXML
    private TableView<Quiz> quizTable;

    @FXML
    private TableColumn<Quiz, Integer> idColumn;

    @FXML
    private TableColumn<Quiz, String> courseColumn;

    @FXML
    private TableColumn<Quiz, String> titleColumn;

    @FXML
    private TableColumn<Quiz, String> descriptionColumn;

    @FXML
    private TableColumn<Quiz, Integer> durationColumn;

    @FXML
    private TableColumn<Quiz, String> createdAtColumn;

    @FXML
    private TableColumn<Quiz, Float> noteColumn;

    @FXML
    private TableColumn<Quiz, Void> actionColumn;

    @FXML
    private Button addQuizButton;

    private final QuizService quizService = new QuizService();
    private ObservableList<Quiz> quizList;

    @FXML
    private void initialize() {
        // Initialiser les colonnes du tableau
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        courseColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            return new SimpleStringProperty(quiz.getCourse() != null ? quiz.getCourse().getTitle() : "N/A");
        });
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        durationColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDuration()).asObject());
        createdAtColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(quiz.getCreatedAt() != null ? dateFormat.format(quiz.getCreatedAt()) : "N/A");
        });
        noteColumn.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getNote()).asObject());

        // Configurer la colonne "Action"
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final Button viewQuestionsButton = new Button("Voir Questions");
            private final HBox actionButtons = new HBox(10, modifyButton, deleteButton, viewQuestionsButton);

            {
                modifyButton.getStyleClass().add("update-button");
                deleteButton.getStyleClass().add("delete-button");
                viewQuestionsButton.getStyleClass().add("view-button");
                actionButtons.setStyle("-fx-alignment: center;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Quiz quiz = getTableView().getItems().get(getIndex());

                    // Action du bouton "Modifier"
                    modifyButton.setOnAction(event -> goToModifyQuiz(quiz));

                    // Action du bouton "Supprimer"
                    deleteButton.setOnAction(event -> {
                        quizService.delete(quiz);
                        quizList.remove(quiz);
                    });

                    // Action du bouton "Voir Questions"
                    viewQuestionsButton.setOnAction(event -> goToViewQuestions(quiz));

                    setGraphic(actionButtons);
                }
            }
        });

        // Charger les quiz
        loadQuizData();
    }

    private void loadQuizData() {
        try {
            quizList = FXCollections.observableArrayList(quizService.readAll());
            quizTable.setItems(quizList);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }

    @FXML
    private void goToAddQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuizBackend.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /addQuiz.fxml");
            }
            Parent root = loader.load();

            Stage stage = (Stage) addQuizButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ajouter un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'interface d'ajout : " + e.toString());
        }
    }

    private void goToModifyQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuiz.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /updateQuiz.fxml");
            }
            Parent root = loader.load();

            UpdateQuizController controller = loader.getController();
            controller.setQuizToUpdate(quiz);

            Stage stage = (Stage) quizTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Modifier un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'interface de modification : " + e.toString());
        }
    }

    private void goToViewQuestions(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestionBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /afficherQuestions.fxml");
            }
            Parent root = loader.load();

            AfficherQuestionsController controller = loader.getController();
            controller.setQuizId(quiz.getId());

            Stage stage = (Stage) quizTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Questions du Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des questions : " + e.toString());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}