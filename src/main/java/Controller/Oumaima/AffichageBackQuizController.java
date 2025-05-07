package Controller.Oumaima;

import entite.Oumaima.Quiz;
import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.Oumaima.QuizService;
import test.Sidebar;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class AffichageBackQuizController {

    @FXML private TableView<Quiz> quizTable;
    @FXML private TableColumn<Quiz, Integer> idColumn;
    @FXML private TableColumn<Quiz, String> courseColumn;
    @FXML private TableColumn<Quiz, String> titleColumn;
    @FXML private TableColumn<Quiz, String> descriptionColumn;
    @FXML private TableColumn<Quiz, Integer> durationColumn;
    @FXML private TableColumn<Quiz, String> createdAtColumn;
    @FXML private TableColumn<Quiz, Float> noteColumn;
    @FXML private TableColumn<Quiz, Void> actionColumn;
    @FXML private Button addQuizButton;

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
            private final Button modifyButton = new Button();
            private final Button deleteButton = new Button();
            private final Button viewQuestionsButton = new Button();
            private final HBox actionButtons = new HBox(10, modifyButton, deleteButton, viewQuestionsButton);

            {
                setupButton(modifyButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Modify Quiz");
                setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete Quiz");
                setupButton(viewQuestionsButton, "https://img.icons8.com/?size=100&id=114896&format=png&color=000000", "View Questions");
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

        // Configure addQuizButton with icon
        Platform.runLater(() -> {
            setupButton(addQuizButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Add Quiz");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuizBackend.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /addQuizBackend.fxml");
            }
            Parent fxmlContent = loader.load();

            Stage stage = (Stage) addQuizButton.getScene().getWindow();

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
            stage.setTitle("Ajouter un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'interface d'ajout : " + e.toString());
        }
    }

    private void goToModifyQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuizBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /updateQuizBack.fxml");
            }
            Parent fxmlContent = loader.load();

            UpdateQuizBck controller = loader.getController();
            controller.setQuizToUpdate(quiz);

            Stage stage = (Stage) quizTable.getScene().getWindow();

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
            stage.setTitle("Modifier un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'interface de modification : " + e.toString());
        }
    }

    private void goToViewQuestions(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/afficherQuestionBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /OumaimaFXML/afficherQuestionBack.fxml");
            }
            Parent fxmlContent = loader.load();

            affichageQuestionBack controller = loader.getController();
            controller.setQuizId(quiz.getId());

            Stage stage = (Stage) quizTable.getScene().getWindow();

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
            stage.setTitle("Questions du Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des questions : " + e.getMessage());
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
            showAlert("Erreur", "Erreur lors du chargement de l'interface : " + e.getMessage());
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}