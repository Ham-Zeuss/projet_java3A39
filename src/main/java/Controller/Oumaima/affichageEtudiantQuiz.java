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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import service.Oumaima.QuizService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.scene.control.ScrollPane;

public class affichageEtudiantQuiz implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private FlowPane quizContainer;

    private QuizService quizService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quizService = new QuizService();
        setupQuizContainer();
        displayQuizzes(quizService.readAll());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterQuizzes(newValue);
        });
    }

    private void setupQuizContainer() {
        // Configuration pour 4 cartes par ligne
        quizContainer.setHgap(20);
        quizContainer.setVgap(30);
        quizContainer.setAlignment(Pos.TOP_CENTER);
        quizContainer.setPrefWrapLength(1260); // (290*4) + (20*3) = 1160 + 60 = 1220 (avec marge)
    }

    private void displayQuizzes(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();
        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(createQuizCard(quiz));
        }
    }

    private VBox createQuizCard(Quiz quiz) {
        VBox card = new VBox(15);
        card.setPrefSize(320, 300); // Augmentez légèrement la hauteur pour le bouton
        card.setMaxSize(320, 300);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");

        // Content VBox
        VBox content = new VBox(15);
        content.getStyleClass().add("content");

        Label titleLabel = new Label(quiz.getTitle());
        titleLabel.getStyleClass().add("heading");

        String description = String.format("Cours: %s\nDurée: %d min\nNote: %.1f",
                quiz.getCourse() != null ? quiz.getCourse().getTitle() : "Non défini",
                quiz.getDuration(),
                quiz.getNote());
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("para");

        // Bouton visible maintenant
        Button takeQuizButton = new Button("Passer Quiz");
        takeQuizButton.getStyleClass().add("quiz-button"); // Utilisez la nouvelle classe CSS
        takeQuizButton.setOnAction(event -> goToQuestionList(quiz.getId()));

        content.getChildren().addAll(titleLabel, descLabel, takeQuizButton);
        card.getChildren().add(content);

        // Gardez aussi le clic sur la carte si besoin
        card.setOnMouseClicked(event -> goToQuestionList(quiz.getId()));

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
            if (quizId <= 0) {
                throw new IllegalArgumentException("ID de quiz invalide");
            }

            // Load the FXML for the question list
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/AfficherQuestionsEtudiant.fxml"));
            Parent root = loader.load();

            AfficherQuestionsEtudiantController controller = loader.getController();
            controller.setQuizId(quizId);

            // Get the current stage
            Stage stage = (Stage) quizContainer.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

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

            // 2. Add body content
            root.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(root);

            // 3. Add footer image
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
                    root.prefHeight(-1) +
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
            stage.setTitle("Questions du Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
            alert.showAndWait();
        }

    }
    public void displayQuizzes1(List<Quiz> quizzes) {
        quizContainer.getChildren().clear(); // Clear old cards

        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(createQuizCard(quiz)); // Create and add new cards
        }
    }
}
