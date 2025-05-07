package Controller.Oumaima;

import entite.Oumaima.Cours;
import entite.Oumaima.Quiz;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.Oumaima.CoursService;
import service.Oumaima.QuizService;
import test.Sidebar;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

public class UpdateQuizBck implements Initializable {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField durationField;
    @FXML private Button returnButton;
    @FXML private ComboBox<Cours> courseComboBox;
    @FXML private Button updateButton;

    private final QuizService quizService = new QuizService();
    private final CoursService courseService = new CoursService();
    private Quiz quizToUpdate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Cours> courses = courseService.readAll();
        courseComboBox.getItems().addAll(courses);

        // Personnalisation de l'affichage dans la ComboBox
        courseComboBox.setCellFactory(_ -> new ListCell<>() {
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

        // Configure buttons with icons
        Platform.runLater(() -> {
            setupButton(returnButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Return to Quiz List");
            setupButton(updateButton, "https://img.icons8.com/?size=100&id=91260&format=png&color=000000", "Update Quiz");
        });
    }

    private void setupButton(Button button, String iconUrl, String tooltipText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            button.setText("");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.getStyleClass().add("icon-button");
        }
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

    @FXML
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
                // Charger la page de la liste des quiz
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageBackQuiz.fxml"));
                if (loader.getLocation() == null) {
                    throw new IOException("Fichier FXML introuvable : /OumaimaFXML/affichageBackQuiz.fxml");
                }
                Parent fxmlContent = loader.load();

                Stage stage = (Stage) updateButton.getScene().getWindow();

                Consumer<String> loadFXMLConsumer = fxmlPath -> loadFXML(stage, fxmlPath);
                Sidebar sidebarCreator = new Sidebar();
                ScrollPane sidebar = sidebarCreator.createSidebar(
                        stage,
                        () -> loadDashboard(stage),
                        () -> loadFXML(stage, "/User/index_user.fxml"),
                        () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                        () -> System.out.println("Logout clicked (implement logout logic here)"),
                        loadFXMLConsumer
                );

                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #F7F7F7;");
                root.setLeft(sidebar);
                root.setCenter(fxmlContent);

                Scene scene = new Scene(root, 1000, 600);
                scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Liste des Quiz");
                stage.show();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageBackQuiz.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /OumaimaFXML/affichageBackQuiz.fxml");
            }
            Parent fxmlContent = loader.load();

            Stage stage = (Stage) returnButton.getScene().getWindow();

            Consumer<String> loadFXMLConsumer = fxmlPath -> loadFXML(stage, fxmlPath);
            Sidebar sidebarCreator = new Sidebar();
            ScrollPane sidebar = sidebarCreator.createSidebar(
                    stage,
                    () -> loadDashboard(stage),
                    () -> loadFXML(stage, "/User/index_user.fxml"),
                    () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                    () -> System.out.println("Logout clicked (implement logout logic here)"),
                    loadFXMLConsumer
            );

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");
            root.setLeft(sidebar);
            root.setCenter(fxmlContent);

            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Liste des Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la liste des quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadFXML(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent fxmlContent = loader.load();

            Consumer<String> loadFXMLConsumer = path -> loadFXML(stage, path);
            Sidebar sidebarCreator = new Sidebar();
            ScrollPane sidebar = sidebarCreator.createSidebar(
                    stage,
                    () -> loadDashboard(stage),
                    () -> loadFXML(stage, "/User/index_user.fxml"),
                    () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                    () -> System.out.println("Logout clicked (implement logout logic here)"),
                    loadFXMLConsumer
            );

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");
            root.setLeft(sidebar);
            root.setCenter(fxmlContent);

            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de l'interface : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadDashboard(Stage stage) {
        Consumer<String> loadFXMLConsumer = fxmlPath -> loadFXML(stage, fxmlPath);
        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                stage,
                () -> loadDashboard(stage),
                () -> loadFXML(stage, "/User/index_user.fxml"),
                () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                () -> System.out.println("Logout clicked (implement logout logic here)"),
                loadFXMLConsumer
        );

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #F7F7F7;");

        Label headerLabel = new Label("Analytics dashboard");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.BLACK);

        Label subHeaderLabel = new Label("Demographic properties of your customer");
        subHeaderLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subHeaderLabel.setTextFill(Color.web("#666666"));

        content.getChildren().addAll(headerLabel, subHeaderLabel);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");
        root.setLeft(sidebar);
        root.setCenter(content);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        stage.setScene(scene);
    }
}