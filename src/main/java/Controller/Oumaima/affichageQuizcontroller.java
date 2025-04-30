package Controller.Oumaima;

import entite.Oumaima.Quiz;
import entite.Oumaima.QuizResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
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
    private ScrollPane scrollPane;

    @FXML
    private FlowPane quizContainer;

    private QuizService quizService;
    private QuizResultService quizResultService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quizService = new QuizService();
        quizResultService = new QuizResultService();
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
        card.setPrefWidth(600); // Augmenter la largeur pour mieux afficher les boutons
        card.getStyleClass().add("quiz-card"); // Appliquer la classe CSS

        Label titleLabel = new Label(quiz.getTitle());
        titleLabel.getStyleClass().add("quiz-card-title"); // Appliquer la classe CSS

        Label courseLabel = new Label("Cours: " + (quiz.getCourse() != null ? quiz.getCourse().getTitle() : "Non défini"));
        courseLabel.getStyleClass().add("quiz-card-label"); // Appliquer la classe CSS

        Label durationLabel = new Label("Durée: " + quiz.getDuration() + " min");
        durationLabel.getStyleClass().add("quiz-card-label"); // Appliquer la classe CSS

        Label noteLabel = new Label("Note: " + quiz.getNote());
        noteLabel.getStyleClass().add("quiz-card-label"); // Appliquer la classe CSS

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button"); // Appliquer la classe CSS
        deleteButton.setOnAction(event -> {
            quizService.delete(quiz);
            quizContainer.getChildren().remove(card);
        });

        Button updateButton = new Button("Modifier");
        updateButton.getStyleClass().add("update-button"); // Appliquer la classe CSS
        updateButton.setOnAction(event -> goToUpdateQuiz(quiz));

        Button viewQuestionsButton = new Button("Voir les questions");
        viewQuestionsButton.getStyleClass().add("view-button"); // Appliquer la classe CSS
        viewQuestionsButton.setOnAction(event -> goToQuestionList(quiz.getId()));

        // Ajout du bouton "Voir Statistique"
        Button viewStatsButton = new Button("Voir Statistique");
        viewStatsButton.getStyleClass().add("view-button"); // Appliquer la classe CSS
        viewStatsButton.setOnAction(event -> viewStatistics(quiz));

        // Ajouter tous les boutons dans le conteneur HBox
        HBox buttonContainer = new HBox(updateButton, deleteButton, viewQuestionsButton, viewStatsButton);
        buttonContainer.getStyleClass().add("button-container"); // Appliquer la classe CSS

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

    private void viewStatistics(Quiz quiz) {
        try {
            // Récupérer les résultats des quiz depuis la table quiz_result pour le quiz spécifique
            List<QuizResult> quizResults = quizResultService.readAll().stream()
                    .filter(result -> result.getQuiz().getId() == quiz.getId())
                    .collect(Collectors.toList());

            // Vérifier si des données existent pour ce quiz
            if (quizResults.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune donnée disponible pour les statistiques de ce quiz.");
                alert.showAndWait();
                return;
            }

            // Créer une nouvelle fenêtre (pop-up) pour afficher les statistiques
            Stage statsStage = new Stage();
            statsStage.initModality(Modality.APPLICATION_MODAL);
            statsStage.setTitle("Statistiques du Quiz : " + quiz.getTitle());

            // Conteneur principal pour les statistiques
            VBox statsContainer = new VBox(10);
            statsContainer.setPadding(new javafx.geometry.Insets(20));
            statsContainer.getStyleClass().add("stats-container"); // Appliquer la classe CSS

            // Titre
            Label titleLabel = new Label("Statistiques pour " + quiz.getTitle());
            titleLabel.getStyleClass().add("stats-title"); // Appliquer la classe CSS

            // Conteneur pour les statistiques textuelles
            VBox statsContent = new VBox(0); // Pas d'espacement ici, géré par stats-row

            // Calculer les statistiques pour ce quiz
            // 1. Nombre de tentatives
            int attemptCount = quizResults.size();

            // 2. Note moyenne
            double averageScore = quizResults.stream()
                    .mapToDouble(QuizResult::getNote)
                    .average()
                    .orElse(0.0);

            // 3. Répartition des notes
            Map<Float, Long> scoreDistribution = quizResults.stream()
                    .collect(Collectors.groupingBy(QuizResult::getNote, Collectors.counting()));

            // 4. Nombre d'étudiants distincts
            long uniqueStudents = quizResults.stream()
                    .filter(result -> result.getUser() != null) // Vérification pour éviter NullPointerException
                    .map(result -> result.getUser().getId())
                    .distinct()
                    .count();

            // Créer une carte pour les statistiques textuelles
            VBox quizStatsCard = new VBox(5);
            quizStatsCard.getStyleClass().add("stats-card"); // Appliquer la classe CSS

            Label quizLabel = new Label("Quiz : " + quiz.getTitle());
            quizLabel.getStyleClass().add("quiz-label"); // Appliquer la classe CSS

            // Chaque ligne de statistique dans un HBox avec la classe stats-row
            // Ligne 1 : Nombre de tentatives
            HBox attemptsRow = new HBox(8);
            attemptsRow.getStyleClass().add("stats-row");
            Label attemptsLabel = new Label("\uD83D\uDCCB Nombre de tentatives : " + attemptCount); // 📋
            attemptsLabel.getStyleClass().add("stats-label");
            attemptsLabel.getStyleClass().add("attempts");
            attemptsRow.getChildren().add(attemptsLabel);

            // Ligne 2 : Note moyenne
            HBox averageRow = new HBox(8);
            averageRow.getStyleClass().add("stats-row");
            Label averageLabel = new Label("\uD83C\uDFC6 Note moyenne : " + String.format("%.2f", averageScore)); // 🏆
            averageLabel.getStyleClass().add("stats-label");
            averageLabel.getStyleClass().add("average");
            averageRow.getChildren().add(averageLabel);

            // Ligne 3 : Répartition des notes
            HBox distributionRow = new HBox(8);
            distributionRow.getStyleClass().add("stats-row");
            Label distributionLabel = new Label("\uD83D\uDCCA Répartition des notes : " + scoreDistribution.entrySet().stream()
                    .map(e -> "Note " + e.getKey() + " : " + e.getValue() + " fois")
                    .collect(Collectors.joining(", "))); // 📊
            distributionLabel.getStyleClass().add("stats-label");
            distributionLabel.getStyleClass().add("distribution");
            distributionRow.getChildren().add(distributionLabel);

            // Ligne 4 : Nombre d'étudiants
            HBox studentsRow = new HBox(8);
            studentsRow.getStyleClass().add("stats-row");
            Label studentsLabel = new Label("\uD83D\uDC65 Nombre d'étudiants : " + uniqueStudents); // 👥
            studentsLabel.getStyleClass().add("stats-label");
            studentsLabel.getStyleClass().add("students");
            studentsRow.getChildren().add(studentsLabel);

            quizStatsCard.getChildren().addAll(quizLabel, attemptsRow, averageRow, distributionRow, studentsRow);
            statsContent.getChildren().add(quizStatsCard);

            // Créer un BarChart pour visualiser la répartition des notes
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Note");
            yAxis.setLabel("Nombre de tentatives");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Répartition des notes (Barres)");
            barChart.setPrefHeight(200);
            barChart.getStyleClass().add("chart"); // Appliquer la classe CSS

            // Créer une série de données pour le BarChart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Notes");

            // Ajouter les données de répartition des notes au BarChart
            for (Map.Entry<Float, Long> entry : scoreDistribution.entrySet()) {
                series.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue()));
            }

            barChart.getData().add(series);
            barChart.setLegendVisible(false); // Cacher la légende car nous n'avons qu'une série

            // Créer un PieChart pour visualiser la répartition des notes sous forme circulaire
            PieChart pieChart = new PieChart();
            pieChart.setTitle("Répartition des notes (Circulaire)");
            pieChart.setPrefHeight(200);
            pieChart.getStyleClass().add("pie-chart"); // Appliquer la classe CSS

            // Ajouter les données de répartition des notes au PieChart
            for (Map.Entry<Float, Long> entry : scoreDistribution.entrySet()) {
                pieChart.getData().add(new PieChart.Data("Note " + entry.getKey(), entry.getValue()));
            }

            pieChart.setLegendVisible(true); // Afficher la légende pour le PieChart

            // Ajouter les graphiques et les statistiques textuelles au conteneur principal
            statsContainer.getChildren().addAll(titleLabel, statsContent, barChart, pieChart);

            // Créer la scène et afficher la pop-up
            Scene statsScene = new Scene(statsContainer, 400, 650); // Augmenter la hauteur pour les deux graphiques
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

            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuiz.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            UpdateQuizController controller = bodyLoader.getController();
            controller.setQuizToUpdate(quiz);

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

            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuiz.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

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

            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestions.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            AfficherQuestionsController controller = bodyLoader.getController();
            controller.setQuizId(quizId);

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