package Controller.Hedy;
import entite.Rating;
import entite.Cours;
import entite.Module;
import entite.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.CoursService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AffichageCoursController {

    @FXML private Label moduleTitleLabel;
    @FXML private GridPane coursGrid;
    @FXML private ComboBox<String> filterComboBox;

    // 🔥 Add this field to access the "Ajouter Cours" button
    @FXML private Button ajouterCoursButton;

    private Module currentModule;
    private final CoursService coursService = new CoursService();

    private static final String FILTER_ALL = "All Courses";
    private static final String FILTER_CREATED_BY_ME = "Créé par moi";
    private static final String FILTER_RECENTLY_ADDED = "Récemment ajouté";

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            moduleTitleLabel.setText("Cours: " + module.getTitle());
            loadCoursCards();
        }

        Session session = Session.getInstance();
        String userRole = session.getRole();

        // ✅ Hide the "Ajouter Cours" button for parents
        if ("ROLE_PARENT".equals(userRole)) {
            ajouterCoursButton.setVisible(false);
            ajouterCoursButton.setManaged(false);
        } else {
            ajouterCoursButton.setVisible(true);
            ajouterCoursButton.setManaged(true);
        }
    }

    @FXML
    public void initialize() {
        filterComboBox.getItems().addAll(FILTER_ALL, FILTER_CREATED_BY_ME, FILTER_RECENTLY_ADDED);
        filterComboBox.setValue(FILTER_ALL);

        Session session = Session.getInstance();
        String userRole = session.getRole();

        if ("ROLE_PARENT".equals(userRole)) {
            filterComboBox.setVisible(false);
            filterComboBox.setManaged(false);
        } else {
            filterComboBox.setOnAction(event -> applyFilter());
        }
    }

    private void loadCoursCards() {
        coursGrid.getChildren().clear(); // Clear existing cards
        List<Cours> coursList = coursService.getCoursByModule(currentModule.getId());

        Session session = Session.getInstance();
        int currentUserId = session.getUserId();

        // Sort by owner first
        coursList.sort((c1, c2) -> {
            boolean isC1CreatedByUser = c1.getUserId() == currentUserId;
            boolean isC2CreatedByUser = c2.getUserId() == currentUserId;
            if (isC1CreatedByUser && !isC2CreatedByUser) return -1;
            else if (!isC1CreatedByUser && isC2CreatedByUser) return 1;
            else return 0;
        });

        loadCoursCardsFiltered(coursList);
    }

    private void loadCoursCardsFiltered(List<Cours> coursList) {
        coursGrid.getChildren().clear();
        int columns = 3;
        int row = 0;
        int column = 0;

        for (Cours cours : coursList) {
            VBox card = createCoursCard(cours);
            GridPane.setMargin(card, new Insets(10));
            coursGrid.add(card, column, row);
            column++;
            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    private void applyFilter() {
        String selectedFilter = filterComboBox.getValue();
        List<Cours> coursList = coursService.getCoursByModule(currentModule.getId());
        Session session = Session.getInstance();
        int currentUserId = session.getUserId();

        switch (selectedFilter) {
            case FILTER_CREATED_BY_ME:
                coursList.removeIf(c -> c.getUserId() != currentUserId);
                break;

            case FILTER_RECENTLY_ADDED:
                coursList.sort((c1, c2) -> {
                    LocalDateTime updatedAt1 = c1.getUpdatedAt() != null ? c1.getUpdatedAt() : LocalDateTime.MIN;
                    LocalDateTime updatedAt2 = c2.getUpdatedAt() != null ? c2.getUpdatedAt() : LocalDateTime.MIN;
                    return updatedAt2.compareTo(updatedAt1); // Descending
                });
                break;

            default:
                coursList.sort((c1, c2) -> {
                    boolean isC1CreatedByUser = c1.getUserId() == currentUserId;
                    boolean isC2CreatedByUser = c2.getUserId() == currentUserId;
                    if (isC1CreatedByUser && !isC2CreatedByUser) return -1;
                    else if (!isC1CreatedByUser && isC2CreatedByUser) return 1;
                    else return 0;
                });
                break;
        }

        loadCoursCardsFiltered(coursList);
    }

    private VBox createCoursCard(Cours cours) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefSize(300, 180);

        // ✅ Apply only CSS class styling
        card.getStyleClass().add("module-card");

        // Title
        Label titleLabel = new Label(cours.getTitle());
        titleLabel.getStyleClass().add("heading");
        titleLabel.setOnMouseClicked(event -> openPdf(cours));

        // PDF Name
        Label pdfLabel = new Label("PDF: " + cours.getPdfName());
        pdfLabel.getStyleClass().add("para");

        // Updated At
        String updatedAtText = (cours.getUpdatedAt() != null)
                ? "Dernière modification: " + cours.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Dernière modification: N/A";
        Label updatedAtLabel = new Label(updatedAtText);
        updatedAtLabel.getStyleClass().add("para");

        // Stars Box
        Session session = Session.getInstance();
        String userRole = session.getRole();
        int currentUserId = session.getUserId();

        HBox starsBox = new HBox(5);
        if ("ROLE_PARENT".equals(userRole)) {
            Label[] starLabels = new Label[5];
            int existingRating = RatingsStorage.getRatings().stream()
                    .filter(r -> r.getCourseId() == cours.getId() && r.getUserId() == currentUserId)
                    .map(Rating::getRating)
                    .findFirst()
                    .orElse(0);

            for (int i = 0; i < 5; i++) {
                Label star = new Label(i < existingRating ? "★" : "☆");
                star.setStyle("-fx-font-size: 20px; -fx-text-fill: gold;");
                final int ratingValue = i + 1;

                star.setOnMouseClicked(event -> {
                    event.consume();
                    for (int j = 0; j < 5; j++) {
                        starLabels[j].setText(j < ratingValue ? "★" : "☆");
                    }
                    RatingsStorage.addOrUpdateRating(cours.getId(), currentUserId, ratingValue);
                });

                starLabels[i] = star;
                starsBox.getChildren().add(star);
            }
        }

        // Buttons
        HBox buttonBox = new HBox(10);
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");

        editButton.getStyleClass().add("button-Enregistrer");
        deleteButton.getStyleClass().add("button-Reinitialiser");

        boolean isCourseCreatedByUser = cours.getUserId() == currentUserId;
        if (isCourseCreatedByUser) {
            buttonBox.getChildren().addAll(editButton, deleteButton);
        } else {
            buttonBox.setVisible(false);
            buttonBox.setManaged(false);
        }

        editButton.setOnAction(e -> editCours(cours));
        deleteButton.setOnAction(e -> {
            coursService.delete(cours);
            loadCoursCards();
        });

        card.getChildren().addAll(titleLabel, pdfLabel, updatedAtLabel, starsBox, buttonBox);
        return card;
    }

    private void editCours(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/EditCours.fxml"));
            Parent root = loader.load();
            EditCoursController controller = loader.getController();
            controller.setCoursToEdit(cours);
            Stage stage = (Stage) coursGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier un Cours");
        } catch (IOException e) {
            System.err.println("Error loading EditCours.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void retourModules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageModule.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coursGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Modules");
        } catch (IOException e) {
            System.err.println("Error loading AffichageModule.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void goToAjoutCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AjoutCours.fxml"));
            Parent root = loader.load();
            AjoutCoursController controller = loader.getController();
            controller.setCurrentModule(currentModule);

            Session session = Session.getInstance();
            int currentUserId = session.getUserId();
            controller.setCurrentUserId(currentUserId);

            Stage stage = (Stage) coursGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Cours");
        } catch (IOException e) {
            System.err.println("Error loading AjoutCours.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void openPdf(Cours cours) {
        try {
            String dropboxUrl = cours.getPdfName();
            if (dropboxUrl == null || dropboxUrl.isEmpty()) {
                System.err.println("PDF URL is empty or null.");
                return;
            }

            if (dropboxUrl.contains("?dl=0")) {
                dropboxUrl = dropboxUrl.replace("?dl=0", "?raw=1");
            }

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.load(dropboxUrl);

            Stage pdfStage = new Stage();
            pdfStage.setTitle("PDF Preview: " + cours.getTitle());
            pdfStage.setScene(new Scene(webView, 800, 600));
            pdfStage.show();
        } catch (Exception e) {
            System.err.println("Error loading PDF preview: " + e.getMessage());
        }
    }
}