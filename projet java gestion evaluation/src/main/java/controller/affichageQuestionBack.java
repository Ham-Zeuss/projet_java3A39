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
import java.util.List;

public class affichageQuestionBack {

    @FXML
    private TableView<Question> questionTable;

    @FXML
    private TableColumn<Question, String> textColumn;

    @FXML
    private TableColumn<Question, String> typeColumn;

    @FXML
    private TableColumn<Question, String> optionsColumn;

    @FXML
    private TableColumn<Question, Void> actionColumn;

    @FXML
    private Button backButton;

    private final QuestionService questionService = new QuestionService();
    private int quizId;

    public void setQuizId(int quizId) {
        this.quizId = quizId;
        afficherQuestions();
    }

    private void afficherQuestions() {
        questionTable.getItems().clear();

        try {
            List<Question> questions = questionService.getQuestionsByQuizId(quizId);
            if (questions.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune question trouvée pour ce quiz.");
                alert.showAndWait();
                return;
            }

            // Configurer les colonnes
            textColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getText()));
            typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionType()));
            optionsColumn.setCellValueFactory(cellData -> {
                String options = String.join(", ",
                        safeText(cellData.getValue().getOption1()),
                        safeText(cellData.getValue().getOption2()),
                        safeText(cellData.getValue().getOption3()),
                        safeText(cellData.getValue().getOption4())
                );
                return new javafx.beans.property.SimpleStringProperty(options);
            });

            // Colonne Actions
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button updateButton = new Button("Modifier");
                private final Button deleteButton = new Button("Supprimer");
                private final HBox buttonContainer = new HBox(10, updateButton, deleteButton);

                {
                    updateButton.getStyleClass().add("update-button");
                    deleteButton.getStyleClass().add("delete-button");
                    buttonContainer.setStyle("-fx-alignment: center;");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Question question = getTableView().getItems().get(getIndex());
                        updateButton.setOnAction(e -> goToUpdateQuestion(question));
                        deleteButton.setOnAction(e -> {
                            questionService.delete(question);
                            questionTable.getItems().remove(question);
                        });
                        setGraphic(buttonContainer);
                    }
                }
            });

            // Ajouter les questions au tableau
            questionTable.getItems().addAll(questions);
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des questions : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void goToUpdateQuestion(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateQuestionBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /updateQuestionBack.fxml");
            }
            Parent root = loader.load();

            UpdateQuestionBackController controller = loader.getController();
            controller.setQuestionToUpdate(question, quizId);

            Stage stage = (Stage) questionTable.getScene().getWindow();
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

            // Load headerBack.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/headerBack.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load headerBack.html
            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/headerBack.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            // Load body (affichageQuizBack.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/affichageQuizBack.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            // Load footerBack.html
            WebView footerWebView = new WebView();
            URL footerUrl = getClass().getResource("/footerBack.html");
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
            URL cssUrl = getClass().getResource("/stylesBack.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyleBack.css");
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