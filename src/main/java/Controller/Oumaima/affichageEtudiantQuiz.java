package Controller.Oumaima;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class affichageEtudiantQuiz implements Initializable {

    @FXML
    private TextField searchField;

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

        Button takeQuizButton = new Button("Passer Quiz");
        takeQuizButton.getStyleClass().add("view-button");
        takeQuizButton.setOnAction(event -> goToQuestionList(quiz.getId()));

        HBox buttonContainer = new HBox(takeQuizButton);
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

    private void goToQuestionList(int quizId) {
        try {
            // Validation du quizId
            if (quizId <= 0) {
                throw new IllegalArgumentException("L'ID du quiz doit être supérieur à 0");
            }

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            VBox mainContent = new VBox();

            // Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            if (headerFxmlLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /header.fxml introuvable");
            }
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load an image instead of header.html
            ImageView headerImageView = new ImageView();
            URL imageUrl = getClass().getResource("/images/header.png"); // Adjust path if needed
            if (imageUrl != null) {
                Image headerImage = new Image(imageUrl.toExternalForm());
                headerImageView.setImage(headerImage);
            } else {
                headerImageView.setImage(new Image("https://thumbs.dreamstime.com/b/stunning-hd-pic-caribbean-beach-cocktail-featuring-coconut-pineapple-set-against-palm-trees-sand-blue-sea-359953956.jpg"));
            }
            headerImageView.setFitWidth(2000);
            headerImageView.setFitHeight(300);
            headerImageView.setPreserveRatio(false);
            mainContent.getChildren().add(headerImageView);

            // Load body (AfficherQuestionsEtudiant.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/AfficherQuestionsEtudiant.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/AfficherQuestionsEtudiant.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            // Set controller data (utiliser le bon contrôleur)
            AfficherQuestionsEtudiantController controller = bodyLoader.getController();
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
            stage.setTitle("Passer le Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des questions : " + e.getMessage() + "\n" + e.getClass().getName());
            alert.showAndWait();
        }
    }

}