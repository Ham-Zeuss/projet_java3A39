package Controller.Oumaima;

import entite.Oumaima.Quiz;
import entite.Oumaima.QuizResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.Oumaima.QuizResultService;
import service.Oumaima.QuizService;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class affichageQuizcontroller implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private Button addQuizButton;

    @FXML
    private Button returnButton;


    @FXML
    private Button viewStatsButton;

    @FXML
    private Button viewQuestionsButton;

    @FXML
    private Button updateButton;


    @FXML
    private FlowPane quizContainer;

    private QuizService quizService;
    private QuizResultService quizResultService;
    private Quiz currentQuiz;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quizService = new QuizService();
        quizResultService = new QuizResultService();
        setupQuizContainer();
        List<Quiz> quizzes = quizService.readAll();
        displayQuizzes(quizzes);
        // Sélectionner le premier quiz par défaut si la liste n'est pas vide
        if (!quizzes.isEmpty()) {
            currentQuiz = quizzes.get(0);
            System.out.println("Quiz par défaut sélectionné: " + currentQuiz.getTitle());
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterQuizzes(newValue);
        });

        // Configure buttons with icons and text
        setupButton(addQuizButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Ajouter Quiz", true);
        setupButton(returnButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Retour", true);

    }
    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(55);
            icon.setFitHeight(55);
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

    private void setupQuizContainer() {
        quizContainer.setHgap(25);
        quizContainer.setVgap(35);
        quizContainer.setAlignment(Pos.TOP_CENTER);
        quizContainer.setPrefWrapLength(1900);
    }

    private void displayQuizzes(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();
        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(createQuizCard(quiz));
        }
    }

    private VBox createQuizCard(Quiz quiz) {
        VBox card = new VBox(15);
        card.setPrefSize(600, 400); // Taille rectangulaire horizontale
        card.setMaxSize(600, 400);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("pack-card");

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

        HBox buttonContainer = new HBox(20); // Espacement accru
        buttonContainer.getStyleClass().add("button-container");
        buttonContainer.setAlignment(Pos.CENTER); // Centrer les boutons

        Button deleteButton = new Button();
        Button updateButton = new Button();
        Button viewQuestionsButton = new Button();
        Button viewStatsButton = new Button();

        // Configure buttons with icons and text
        setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Supprimer", false);
        setupButton(updateButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Modifier", false);
        setupButton(viewQuestionsButton, "https://img.icons8.com/?size=100&id=114917&format=png&color=000000", "Questions", false);
        setupButton(viewStatsButton, "https://img.icons8.com/?size=100&id=110187&format=png&color=000000", "Statistiques", false);

        deleteButton.setOnAction(event -> {
            quizService.delete(quiz);
            quizContainer.getChildren().remove(card);
        });

        updateButton.setOnAction(event -> goToUpdateQuiz(quiz));

        viewQuestionsButton.setOnAction(event -> goToQuestionList(quiz.getId()));

        viewStatsButton.setOnAction(event -> viewStatistics(quiz));

        buttonContainer.getChildren().addAll(updateButton, deleteButton, viewQuestionsButton, viewStatsButton);
        content.getChildren().addAll(titleLabel, descLabel, buttonContainer);
        card.getChildren().add(content);

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

    private void viewStatistics(Quiz quiz) {
        try {
            List<QuizResult> quizResults = quizResultService.readAll().stream()
                    .filter(result -> result.getQuiz().getId() == quiz.getId())
                    .collect(Collectors.toList());

            if (quizResults.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune donnée disponible pour les statistiques de ce quiz.");
                alert.showAndWait();
                return;
            }

            Stage statsStage = new Stage();
            statsStage.initModality(Modality.APPLICATION_MODAL);
            statsStage.setTitle("Statistiques du Quiz : " + quiz.getTitle());

            VBox statsContainer = new VBox(20);
            statsContainer.setPadding(new Insets(20));
            statsContainer.getStyleClass().add("card");
            statsContainer.setPrefWidth(600);
            statsContainer.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("Statistiques pour " + quiz.getTitle());
            titleLabel.getStyleClass().add("heading");

            VBox statsContent = new VBox(15);
            statsContent.getStyleClass().add("content");

            int attemptCount = quizResults.size();
            double averageScore = quizResults.stream()
                    .mapToDouble(QuizResult::getNote)
                    .average()
                    .orElse(0.0);
            Map<Float, Long> scoreDistribution = quizResults.stream()
                    .collect(Collectors.groupingBy(QuizResult::getNote, Collectors.counting()));
            long uniqueStudents = quizResults.stream()
                    .filter(result -> result.getUser() != null)
                    .map(result -> result.getUser().getId())
                    .distinct()
                    .count();

            VBox quizStatsCard = new VBox(10);
            quizStatsCard.getStyleClass().add("stats-card");

            Label quizLabel = new Label("Quiz : " + quiz.getTitle());
            quizLabel.getStyleClass().add("para");

            HBox attemptsRow = new HBox(8);
            attemptsRow.getStyleClass().add("stats-row");
            Label attemptsLabel = new Label("\uD83D\uDCCB Nombre de tentatives : " + attemptCount);
            attemptsLabel.getStyleClass().add("para");
            attemptsRow.getChildren().add(attemptsLabel);

            HBox averageRow = new HBox(8);
            averageRow.getStyleClass().add("stats-row");
            Label averageLabel = new Label("\uD83C\uDFC6 Note moyenne : " + String.format("%.2f", averageScore));
            averageLabel.getStyleClass().add("para");
            averageRow.getChildren().add(averageLabel);

            HBox distributionRow = new HBox(8);
            distributionRow.getStyleClass().add("stats-row");
            Label distributionLabel = new Label("\uD83D\uDCCA Répartition des notes : " + scoreDistribution.entrySet().stream()
                    .map(e -> "Note " + e.getKey() + " : " + e.getValue() + " fois")
                    .collect(Collectors.joining(", ")));
            distributionLabel.getStyleClass().add("para");
            distributionRow.getChildren().add(distributionLabel);

            HBox studentsRow = new HBox(8);
            studentsRow.getStyleClass().add("stats-row");
            Label studentsLabel = new Label("\uD83D\uDC65 Nombre d'étudiants : " + uniqueStudents);
            studentsLabel.getStyleClass().add("para");
            studentsRow.getChildren().add(studentsLabel);

            quizStatsCard.getChildren().addAll(quizLabel, attemptsRow, averageRow, distributionRow, studentsRow);
            statsContent.getChildren().add(quizStatsCard);

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Note");
            yAxis.setLabel("Nombre de tentatives");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Répartition des notes (Barres)");
            barChart.setPrefHeight(200);
            barChart.getStyleClass().add("chart");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Notes");

            for (Map.Entry<Float, Long> entry : scoreDistribution.entrySet()) {
                series.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue()));
            }

            barChart.getData().add(series);
            barChart.setLegendVisible(false);

            PieChart pieChart = new PieChart();
            pieChart.setTitle("Répartition des notes (Circulaire)");
            pieChart.setPrefHeight(200);
            pieChart.getStyleClass().add("pie-chart");

            for (Map.Entry<Float, Long> entry : scoreDistribution.entrySet()) {
                pieChart.getData().add(new PieChart.Data("Note " + entry.getKey(), entry.getValue()));
            }

            pieChart.setLegendVisible(true);

            statsContent.getChildren().addAll(barChart, pieChart);
            statsContainer.getChildren().addAll(titleLabel, statsContent);

            Scene statsScene = new Scene(statsContainer, 600, 650);
            URL cssUrl = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (cssUrl != null) {
                statsScene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Erreur : Fichier CSS oumaimastyle.css non trouvé.");
            }

            statsStage.setScene(statsScene);
            statsStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des statistiques : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void goToUpdateQuiz(Quiz quiz) {
        try {
            Stage stage = (Stage) addQuizButton.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load navbar (header.fxml)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(1500, 100);
            VBox.setMargin(headerFxmlContent, new Insets(0, 0, 10, 0));
            mainContent.getChildren().add(headerFxmlContent);

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

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/updateQuiz.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            UpdateQuizController controller = bodyLoader.getController();
            controller.setQuizToUpdate(quiz);

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

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            URL cssUrl = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) {
                scene.getStylesheets().add(navBarCss.toExternalForm());
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
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load navbar (header.fxml)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(1500, 100);
            VBox.setMargin(headerFxmlContent, new Insets(0, 0, 10, 0));
            mainContent.getChildren().add(headerFxmlContent);

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

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/addQuiz.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

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

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            URL cssUrl = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) {
                scene.getStylesheets().add(navBarCss.toExternalForm());
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
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load navbar (header.fxml)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(1500, 100);
            VBox.setMargin(headerFxmlContent, new Insets(0, 0, 10, 0));
            mainContent.getChildren().add(headerFxmlContent);

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

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestions.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/afficherQuestions.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            AfficherQuestionsController controller = bodyLoader.getController();
            controller.setQuizId(quizId);

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

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            URL cssUrl = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des questions : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void goToCoursFront() {
        try {
            Stage stage = (Stage) returnButton.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load navbar (header.fxml)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(1500, 100);
            VBox.setMargin(headerFxmlContent, new Insets(0, 0, 10, 0));
            mainContent.getChildren().add(headerFxmlContent);

            // Load header image
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

            // Load body content (module list)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageModule.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /HedyFXML/AffichageModule.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // Load footer image
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

            // Configure ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Create scene
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Load CSS files
            URL cssUrl = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) {
                scene.getStylesheets().add(navBarCss.toExternalForm());
            }
            URL storeCardsCss = getClass().getResource("/css/store-cards.css");
            if (storeCardsCss != null) {
                scene.getStylesheets().add(storeCardsCss.toExternalForm());
            }

            // Display scene
            stage.setScene(scene);
            stage.setTitle("Modules");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la page des modules : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public QuizService getQuizService() {
        return quizService;
    }

    public void displayQuizzes1(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();
        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(createQuizCard(quiz));
        }
    }
}