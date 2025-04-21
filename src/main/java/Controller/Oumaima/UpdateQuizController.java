package controller.Oumaima;

import entite.Oumaima.Cours;
import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.Oumaima.CoursService;
import service.Oumaima.QuizService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UpdateQuizController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField durationField;

    @FXML
    private Button returnButton;

    @FXML
    private ComboBox<Cours> courseComboBox;

    @FXML
    private Button updateButton;

    private final QuizService quizService = new QuizService();
    private final CoursService courseService = new CoursService();

    private Quiz quizToUpdate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Cours> courses = courseService.readAll();
        courseComboBox.getItems().addAll(courses);

        // Personnalisation de l'affichage dans la ComboBox
        courseComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });
        courseComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });

        // Action du bouton update
        updateButton.setOnAction(event -> updateQuiz());
    }

    public void setQuizToUpdate(Quiz quiz) {
        this.quizToUpdate = quiz;

        titleField.setText(quiz.getTitle());
        descriptionArea.setText(quiz.getDescription());
        durationField.setText(String.valueOf(quiz.getDuration()));

        if (quiz.getCourse() != null) {
            courseComboBox.getSelectionModel().select(quiz.getCourse());
        }
    }

    private void updateQuiz() {
        if (quizToUpdate != null) {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            int duration;
            try {
                duration = Integer.parseInt(durationField.getText());
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "La durée doit être un nombre valide.");
                alert.showAndWait();
                return;
            }
            Cours selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();

            // Update the quiz object
            quizToUpdate.setTitle(title);
            quizToUpdate.setDescription(description);
            quizToUpdate.setDuration(duration);
            quizToUpdate.setCourse(selectedCourse);

            // Save the updated quiz
            quizService.update(quizToUpdate);

            try {
                // Get the current stage
                Stage currentStage = (Stage) updateButton.getScene().getWindow();
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

                // Load body (affichageQuiz.fxml)
                FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
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

                currentStage.setScene(scene);
                currentStage.setTitle("Liste des Quiz");
                currentStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la liste des quiz : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void returnToAffichageQuiz() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) returnButton.getScene().getWindow();
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

            // Load body (affichageQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
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

            currentStage.setScene(scene);
            currentStage.setTitle("Liste des Quiz");
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la liste des quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }
}