package Controller.Oumaima;

import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import service.Oumaima.QuizService;
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
        card.setPadding(new Insets(20));
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
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            if (headerFxmlLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /header.fxml introuvable");
            }
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // 2. Add header image right below the header.fxml content
            ImageView headerImageView = new ImageView();
            try {
                // Load the header image from resources
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);

                // Set image properties
                headerImageView.setPreserveRatio(true); // Correct method for JavaFX ImageView
                headerImageView.setFitWidth(1500); // Match header width
                headerImageView.setSmooth(true);   // Better quality when scaling
                headerImageView.setCache(true);    // Better performance

                // Add some spacing between header and image if needed
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                // Fallback if image fails to load
                Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 3. Load body (AfficherQuestionsEtudiant.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/AfficherQuestionsEtudiant.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/AfficherQuestionsEtudiant.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // Set controller data
            AfficherQuestionsEtudiantController controller = bodyLoader.getController();
            controller.setQuizId(quizId);

            // 4. Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true); // Correct method for JavaFX ImageView
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
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable vertical scrollbar

            // Calculate required height
            double totalHeight = headerFxmlContent.getPrefHeight() +
                    headerImageView.getFitHeight() +
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
            stage.setTitle("Passer le Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des questions : " + e.getMessage() + "\n" + e.getClass().getName());
            alert.showAndWait();
        }
    }
}