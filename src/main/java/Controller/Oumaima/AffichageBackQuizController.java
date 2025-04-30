package Controller.Oumaima;

import entite.Oumaima.Quiz;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.Oumaima.QuizService;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.io.IOException;
import java.text.SimpleDateFormat;

import test.Sidebar;
import java.util.function.Consumer;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

public class AffichageBackQuizController {

    @FXML
    private TableView<Quiz> quizTable;

    @FXML
    private TableColumn<Quiz, Integer> idColumn;

    @FXML
    private TableColumn<Quiz, String> courseColumn;

    @FXML
    private TableColumn<Quiz, String> titleColumn;

    @FXML
    private TableColumn<Quiz, String> descriptionColumn;

    @FXML
    private TableColumn<Quiz, Integer> durationColumn;

    @FXML
    private TableColumn<Quiz, String> createdAtColumn;

    @FXML
    private TableColumn<Quiz, Float> noteColumn;

    @FXML
    private TableColumn<Quiz, Void> actionColumn;

    @FXML
    private Button addQuizButton;

    private final QuizService quizService = new QuizService();
    private ObservableList<Quiz> quizList;

    @FXML
    private void initialize() {
        // Initialiser les colonnes du tableau
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        courseColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            return new SimpleStringProperty(quiz.getCourse() != null ? quiz.getCourse().getTitle() : "N/A");
        });
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        durationColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDuration()).asObject());
        createdAtColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(quiz.getCreatedAt() != null ? dateFormat.format(quiz.getCreatedAt()) : "N/A");
        });
        noteColumn.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getNote()).asObject());

        // Configurer la colonne "Action"
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final Button viewQuestionsButton = new Button("Voir Questions");
            private final HBox actionButtons = new HBox(10, modifyButton, deleteButton, viewQuestionsButton);

            {
                modifyButton.getStyleClass().add("update-button");
                deleteButton.getStyleClass().add("delete-button");
                viewQuestionsButton.getStyleClass().add("view-button");
                actionButtons.setStyle("-fx-alignment: center;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Quiz quiz = getTableView().getItems().get(getIndex());

                    // Action du bouton "Modifier"
                    modifyButton.setOnAction(event -> goToModifyQuiz(quiz));

                    // Action du bouton "Supprimer"
                    deleteButton.setOnAction(event -> {
                        quizService.delete(quiz);
                        quizList.remove(quiz);
                    });

                    // Action du bouton "Voir Questions"
                    viewQuestionsButton.setOnAction(event -> goToViewQuestions(quiz));

                    setGraphic(actionButtons);
                }
            }
        });

        // Charger les quiz
        loadQuizData();
    }

    private void loadQuizData() {
        try {
            quizList = FXCollections.observableArrayList(quizService.readAll());
            quizTable.setItems(quizList);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }

    @FXML
    private void goToAddQuiz() {
        try {
            // Étape 1 : Charger la page pour ajouter un quiz
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuizBackend.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /addQuiz.fxml");
            }
            Parent fxmlContent = loader.load();

            // Étape 2 : Récupérer la fenêtre actuelle
            Stage stage = (Stage) addQuizButton.getScene().getWindow();

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
            stage.setTitle("Ajouter un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'interface d'ajout : " + e.toString());
        }
    }

    private void goToModifyQuiz(Quiz quiz) {
        try {
            // Étape 1 : Charger la page pour modifier un quiz
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuizBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /updateQuizBack.fxml");
            }
            Parent fxmlContent = loader.load();

            // Dire à la page quel quiz modifier
            UpdateQuizBck controller = loader.getController(); // Use UpdateQuizBack instead of UpdateQuizController
            controller.setQuizToUpdate(quiz);

            // Étape 2 : Récupérer la fenêtre actuelle
            Stage stage = (Stage) quizTable.getScene().getWindow();

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
            stage.setTitle("Modifier un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'interface de modification : " + e.toString());
        }
    }
    private void goToViewQuestions(Quiz quiz) {
        try {
            // Étape 1 : Charger la page des questions (comme avant)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestionBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /OumaimaFXML/afficherQuestionBack.fxml");
            }
            Parent fxmlContent = loader.load();

            // Dire à la page de montrer les questions du quiz sélectionné
            affichageQuestionBack controller = loader.getController();
            controller.setQuizId(quiz.getId());

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) quizTable.getScene().getWindow();

            // Étape 2 : Créer la barre latérale
            // On a besoin d'une fonction pour charger des pages (on l'expliquera plus tard)
            Consumer<String> loadFXMLConsumer = fxmlPath -> loadFXML(stage, fxmlPath);

            // Créer la barre latérale avec la classe Sidebar
            Sidebar sidebarCreator = new Sidebar();
            ScrollPane sidebar = sidebarCreator.createSidebar(
                    stage, // La fenêtre
                    () -> loadDashboard(stage), // Bouton "Dashboard"
                    () -> loadFXML(stage, "/User/index_user.fxml"), // Bouton "Utilisateurs"
                    () -> loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml"), // Bouton "Pixel Words"
                    () -> System.out.println("Logout clicked (implement logout logic here)"), // Bouton "Logout"
                    loadFXMLConsumer // Fonction pour charger des pages
            );

            // Étape 3 : Créer un espace (une table) pour mettre la barre latérale et la page des questions
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;"); // Fond gris clair
            root.setLeft(sidebar); // Mettre la barre latérale à gauche
            root.setCenter(fxmlContent); // Mettre la page des questions au centre

            // Étape 4 : Afficher l'espace dans la fenêtre
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Questions du Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des questions : " + e.getMessage());
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
            showAlert("Erreur", "Erreur lors du chargement de l'interface : " + e.getMessage());
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}