package controller;

import entite.Cours;
import entite.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.CoursService;
import service.QuizService;

import java.io.IOException;
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
            int duration = Integer.parseInt(durationField.getText());
            Cours selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();

            // Update the quiz object
            quizToUpdate.setTitle(title);
            quizToUpdate.setDescription(description);
            quizToUpdate.setDuration(duration);
            quizToUpdate.setCourse(selectedCourse);

            // Save the updated quiz
            quizService.update(quizToUpdate);

            try {
                // Load the AffichageQuiz FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichageQuiz.fxml"));
                Parent root = loader.load();

                // Create a new scene with the loaded FXML
                Scene scene = new Scene(root);

                // Get the current stage (window)
                Stage currentStage = (Stage) updateButton.getScene().getWindow();

                // Set the new scene to the stage
                currentStage.setScene(scene);
                currentStage.setTitle("Liste des Quiz");
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la liste des quiz : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    @FXML
    private void returnToAffichageQuiz() {
        try {
            // Load the AffichageQuiz FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichageQuiz.fxml"));
            Parent root = loader.load();

            // Create a new scene with the loaded FXML
            Scene scene = new Scene(root);

            // Get the current stage (window)
            Stage currentStage = (Stage) returnButton.getScene().getWindow();

            // Set the new scene to the stage
            currentStage.setScene(scene);
            currentStage.setTitle("Liste des Quiz");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la liste des quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }
}
