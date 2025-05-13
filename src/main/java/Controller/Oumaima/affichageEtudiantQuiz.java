package Controller.Oumaima;

import entite.Oumaima.Quiz;
import entite.Session;
import entite.Oumaima.QuizResult;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import service.Oumaima.QuizService;
import service.Oumaima.QuizResultService;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.stage.Screen;

public class affichageEtudiantQuiz implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private Button returnButton;

    @FXML
    private FlowPane quizContainer;

    private QuizService quizService;
    private QuizResultService quizResultService;
    private int courseId = -1; // Variable pour stocker le courseId

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quizService = new QuizService();
        quizResultService = new QuizResultService();
        setupQuizContainer();

        // Afficher les quiz en fonction du courseId
        List<Quiz> quizzes;
        if (courseId != -1) {
            quizzes = quizService.readByCourseId(courseId); // Méthode à implémenter dans QuizService
        } else {
            quizzes = quizService.readAll(); // Fallback si courseId n'est pas défini
        }
        displayQuizzes(quizzes);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterQuizzes(newValue);
        });

        // Configure returnButton with icon and text
        setupButton(returnButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Retour", true);
    }

    // Méthode pour définir le courseId
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    private void setupQuizContainer() {
        quizContainer.setHgap(20);
        quizContainer.setVgap(30);
        quizContainer.setAlignment(Pos.TOP_CENTER);
        quizContainer.setPrefWrapLength(1260);
    }

    private void displayQuizzes(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();
        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(createQuizCard(quiz));
        }
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

    private VBox createQuizCard(Quiz quiz) {
        VBox card = new VBox(15);
        card.setPrefSize(320, 300); // Augmentez légèrement la hauteur pour le bouton
        card.setMaxSize(320, 300);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("pack-card");

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
        Button takeQuizButton = new Button();
        // Configure button with icon and text
        setupButton(takeQuizButton, "https://img.icons8.com/?size=100&id=112158&format=png&color=000000", "Passer Quiz", true);
        takeQuizButton.setOnAction(event -> goToQuestionList(quiz.getId()));

        content.getChildren().addAll(titleLabel, descLabel, takeQuizButton);
        card.getChildren().add(content);

        // Gardez aussi le clic sur la carte si besoin
        card.setOnMouseClicked(event -> goToQuestionList(quiz.getId()));

        return card;
    }

    private void filterQuizzes(String searchText) {
        List<Quiz> allQuizzes;
        if (courseId != -1) {
            allQuizzes = quizService.readByCourseId(courseId);
        } else {
            allQuizzes = quizService.readAll();
        }
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

            // Vérifier si l'utilisateur est un étudiant et s'il a déjà passé le quiz
            Session session = Session.getInstance();
            String userRole = session.getRole();
            int currentUserId = session.getUserId();

            if ("ROLE_PARENT".equals(userRole)) {
                // Vérifier si un QuizResult existe pour cet utilisateur et ce quiz
                List<QuizResult> quizResults = quizResultService.readAll();
                boolean hasTakenQuiz = quizResults.stream()
                        .anyMatch(result -> result.getQuiz().getId() == quizId && result.getUser().getId() == currentUserId);

                if (hasTakenQuiz) {
                    // Si l'étudiant a déjà passé le quiz, afficher une alerte et arrêter
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Vous avez déjà passé ce quiz.");
                    alert.showAndWait();
                    return;
                }
            }

            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            // Load the FXML for the question list
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/AfficherQuestionsEtudiant.fxml"));
            Parent root = loader.load();

            AfficherQuestionsEtudiantController controller = loader.getController();
            controller.setQuizId(quizId);

            // Get the current stage
            Stage stage = (Stage) quizContainer.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load and add navbar (header.fxml)
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

            // Add body content
            root.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            root.getStyleClass().add("body-content");
            mainContent.getChildren().add(root);

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
            stage.setTitle("Questions du Quiz");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void goToCoursFront() {
        try {
            System.out.println("Début de goToCoursFront");

            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            Stage stage = (Stage) returnButton.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load and add header.fxml (Navbar)
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
                System.err.println("Erreur lors du chargement de l'image d'en-tête : " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Image d'en-tête introuvable");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load main content (FXML)
            URL fxmlUrl = getClass().getResource("/HedyFXML/AffichageModule.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("Fichier /HedyFXML/AffichageCoursFront.fxml introuvable");
            }
            System.out.println("Chargement de /HedyFXML/AffichageCoursFront.fxml");
            FXMLLoader bodyLoader = new FXMLLoader(fxmlUrl);
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
                System.err.println("Erreur lors du chargement de l'image de pied de page : " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Image de pied de page introuvable");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Configure ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Create scene
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) scene.getStylesheets().add(navBarCss.toExternalForm());
            URL oumaimaStyle = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (oumaimaStyle != null) scene.getStylesheets().add(oumaimaStyle.toExternalForm());
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
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

            // Display scene
            stage.setScene(scene);
            stage.setTitle("Cours");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la page des cours : " + e.getMessage() + "\nCause: " + e.getCause());
            alert.showAndWait();
        }
    }

    public void displayQuizzes1(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();
        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(createQuizCard(quiz));
        }
    }
}