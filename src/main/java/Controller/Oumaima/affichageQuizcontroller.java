package controller.Oumaima;

import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.Oumaima.QuizService;
import javafx.scene.web.WebView;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class affichageQuizcontroller implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private Button addQuizButton;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private FlowPane quizContainer;

    private QuizService quizService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quizService = new QuizService();
        displayQuizzes(quizService.readAll());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterQuizzes(newValue);
        });
    }

    private void displayQuizzes(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();
        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(createQuizCard(quiz));
        }
    }

    private VBox createQuizCard(Quiz quiz) {
        VBox card = new VBox(12);
        card.setPrefWidth(500);
        card.setPadding(new javafx.geometry.Insets(20));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);");
        card.getStyleClass().add("quiz-card");

        Label titleLabel = new Label(quiz.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        titleLabel.setWrapText(true);

        Label courseLabel = new Label("Cours: " + (quiz.getCourse() != null ? quiz.getCourse().getTitle() : "Non défini"));
        courseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");

        Label durationLabel = new Label("Durée: " + quiz.getDuration() + " min");
        durationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");

        Label noteLabel = new Label("Note: " + quiz.getNote());
        noteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> {
            quizService.delete(quiz);
            quizContainer.getChildren().remove(card);
        });

        Button updateButton = new Button("Modifier");
        updateButton.getStyleClass().add("update-button");
        updateButton.setOnAction(event -> goToUpdateQuiz(quiz));

        Button viewQuestionsButton = new Button("Voir les questions");
        viewQuestionsButton.getStyleClass().add("view-button");
        viewQuestionsButton.setOnAction(event -> goToQuestionList(quiz.getId()));

        HBox buttonContainer = new HBox(updateButton, deleteButton, viewQuestionsButton);
        buttonContainer.setSpacing(10);
        buttonContainer.setStyle("-fx-alignment: center;");

        card.getChildren().addAll(titleLabel, courseLabel, durationLabel, noteLabel, buttonContainer);
        return card;
    }

    private void filterQuizzes(String searchText) {
        List<Quiz> allQuizzes = quizService.readAll();
        List<Quiz> filteredQuizzes = allQuizzes.stream()
                .filter(quiz -> quiz.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
        displayQuizzes(filteredQuizzes);
    }

    private void goToUpdateQuiz(Quiz quiz) {
        try {
            Stage stage = (Stage) addQuizButton.getScene().getWindow();
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

            // Load body (updateQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuiz.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            // Set controller data
            UpdateQuizController controller = bodyLoader.getController();
            controller.setQuizToUpdate(quiz);

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
            stage.setTitle("Modifier le Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement du quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void goToAddQuiz() {
        try {
            Stage stage = (Stage) addQuizButton.getScene().getWindow();
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

            // Load body (addQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuiz.fxml"));
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
            stage.setTitle("Ajouter un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de l'ajout de quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void goToQuestionList(int quizId) {
        try {
            Stage stage = (Stage) addQuizButton.getScene().getWindow();
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

            // Load body (afficherQuestions.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestions.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            // Set controller data
            AfficherQuestionsController controller = bodyLoader.getController();
            controller.setQuizId(quizId);

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
            stage.setTitle("Questions du Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des questions : " + e.getMessage());
            alert.showAndWait();
        }
    }
}