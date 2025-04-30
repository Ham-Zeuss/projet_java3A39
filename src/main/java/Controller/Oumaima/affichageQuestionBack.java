package Controller.Oumaima;

import entite.Oumaima.Question;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.Oumaima.QuestionService;

import java.io.IOException;
import java.util.List;

import test.Sidebar;
import java.util.function.Consumer;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class affichageQuestionBack {

    @FXML
    private TableView<Question> questionTable;

    @FXML
    private TableColumn<Question, String> textColumn;

    @FXML
    private TableColumn<Question, String> typeColumn;

    @FXML
    private TableColumn<Question, String> optionsColumn;

    @FXML
    private TableColumn<Question, Void> actionColumn;

    @FXML
    private Button backButton;

    private final QuestionService questionService = new QuestionService();
    private int quizId;

    public void setQuizId(int quizId) {
        this.quizId = quizId;
        afficherQuestions();
    }

    private void afficherQuestions() {
        questionTable.getItems().clear();

        try {
            List<Question> questions = questionService.getQuestionsByQuizId(quizId);
            if (questions.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune question trouvée pour ce quiz.");
                return;
            }

            // Configurer les colonnes
            textColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getText()));
            typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOptionType()));
            optionsColumn.setCellValueFactory(cellData -> {
                String options = String.join(", ",
                        safeText(cellData.getValue().getOption1()),
                        safeText(cellData.getValue().getOption2()),
                        safeText(cellData.getValue().getOption3()),
                        safeText(cellData.getValue().getOption4())
                );
                return new javafx.beans.property.SimpleStringProperty(options);
            });

            // Ajouter une infobulle pour les options longues
            optionsColumn.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item.length() > 50 ? item.substring(0, 50) + "..." : item);
                        Tooltip tooltip = new Tooltip(item);
                        setTooltip(tooltip);
                    }
                }
            });

            // Colonne Actions
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button updateButton = new Button("Modifier");
                private final Button deleteButton = new Button("Supprimer");
                private final HBox buttonContainer = new HBox(10, updateButton, deleteButton);

                {
                    updateButton.getStyleClass().add("update-button");
                    deleteButton.getStyleClass().add("delete-button");
                    buttonContainer.setStyle("-fx-alignment: center;");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Question question = getTableView().getItems().get(getIndex());
                        updateButton.setOnAction(e -> goToUpdateQuestion(question));
                        deleteButton.setOnAction(e -> {
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette question ?");
                            confirm.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    questionService.delete(question);
                                    questionTable.getItems().remove(question);
                                }
                            });
                        });
                        setGraphic(buttonContainer);
                    }
                }
            });

            // Ajouter les questions au tableau
            questionTable.getItems().addAll(questions);
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des questions : " + e.getMessage());
        }
    }

    private void goToUpdateQuestion(Question question) {
        try {
            // Étape 1 : Charger la page pour modifier une question
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuestionBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /OumaimaFXML/updateQuestionBack.fxml");
            }
            Parent fxmlContent = loader.load();

            // Étape 2 : Dire à la page quelle question modifier
            UpdateQuestionBackController controller = loader.getController();
            controller.setQuestionToUpdate(question, quizId);

            // Étape 3 : Récupérer la fenêtre actuelle
            Stage stage = (Stage) questionTable.getScene().getWindow();

            // Étape 4 : Créer la barre latérale
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

            // Étape 5 : Créer un espace pour mettre la barre latérale et la page
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F7F7F7;");
            root.setLeft(sidebar);
            root.setCenter(fxmlContent);

            // Étape 6 : Afficher l'espace dans la fenêtre
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Modifier une Question");
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void goBackToQuizList() {
        try {
            // Étape 1 : Charger la page de la liste des quiz
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageBackQuiz.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : /OumaimaFXML/affichageQuizBack.fxml");
            }
            Parent fxmlContent = loader.load();

            // Étape 2 : Récupérer la fenêtre actuelle
            Stage stage = (Stage) backButton.getScene().getWindow();

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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la liste des quiz : " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'interface : " + e.getMessage());
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

    private String safeText(String text) {
        return text != null && !text.trim().isEmpty() ? text : "(option vide)";
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}