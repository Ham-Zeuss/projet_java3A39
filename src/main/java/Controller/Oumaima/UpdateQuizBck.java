package Controller.Oumaima;

import entite.Oumaima.Cours;
import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.Oumaima.CoursService;
import service.Oumaima.QuizService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import test.Sidebar;
import java.util.function.Consumer;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

public class UpdateQuizBck implements Initializable {
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
                // Étape 1 : Charger la page de la liste des quiz
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageBackQuiz.fxml"));
                if (loader.getLocation() == null) {
                    throw new IOException("Fichier FXML introuvable : /OumaimaFXML/affichageBackQuiz.fxml");
                }
                Parent fxmlContent = loader.load();

                // Étape 2 : Récupérer la fenêtre actuelle
                Stage stage = (Stage) updateButton.getScene().getWindow();

                // Étape 3 : Créer la barre latérale
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

                // Étape 4 : Créer un espace pour mettre la barre latérale et la page
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #F7F7F7;");
                root.setLeft(sidebar);
                root.setCenter(fxmlContent);

                // Étape 5 : Afficher l'espace dans la fenêtre
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
            // Étape 1 : Charger la page de la liste des quiz
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageBackQuiz.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /OumaimaFXML/affichageBackQuiz.fxml");
            }
            Parent fxmlContent = loader.load();

            // Étape 2 : Récupérer la fenêtre actuelle
            Stage stage = (Stage) returnButton.getScene().getWindow();

            // Étape 3 : Créer la barre latérale
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

            // Étape 4 : Créer un espace pour mettre la barre latérale et la page
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");
            root.setLeft(sidebar);
            root.setCenter(fxmlContent);

            // Étape 5 : Afficher l'espace dans la fenêtre
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
            // Charger la page demandée (fxmlPath)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent fxmlContent = loader.load();

            // Créer une fonction pour charger d'autres pages
            Consumer<String> loadFXMLConsumer = path -> loadFXML(stage, path);

            // Créer la barre latérale
            Sidebar sidebarCreator = new Sidebar();
            ScrollPane sidebar = sidebarCreator.createSidebar(
                    stage,
                    () -> loadDashboard(stage),
                    () -> loadFXML(stage, "/User/index_user.fxml"),
                    () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                    () -> System.out.println("Logout clicked (implement logout logic here)"),
                    loadFXMLConsumer
            );

            // Créer un espace pour mettre la barre latérale et la page
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");
            root.setLeft(sidebar);
            root.setCenter(fxmlContent);

            // Afficher l'espace dans la fenêtre
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
        // Créer une fonction pour charger des pages
        Consumer<String> loadFXMLConsumer = fxmlPath -> loadFXML(stage, fxmlPath);

        // Créer la barre latérale
        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                stage,
                () -> loadDashboard(stage),
                () -> loadFXML(stage, "/User/index_user.fxml"),
                () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"),
                () -> System.out.println("Logout clicked (implement logout logic here)"),
                loadFXMLConsumer
        );

        // Créer un contenu simple pour le tableau de bord
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

        // Créer un espace pour mettre la barre latérale et le contenu
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");
        root.setLeft(sidebar);
        root.setCenter(content);

        // Afficher l'espace dans la fenêtre
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        stage.setScene(scene);
    }
}