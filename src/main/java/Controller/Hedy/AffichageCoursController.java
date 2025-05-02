package Controller.Hedy;
import entite.Rating;
import entite.Cours;
import entite.Module;
import entite.Session; // Import the Session class
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CoursService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;



public class AffichageCoursController {

    @FXML private Label moduleTitleLabel;
    @FXML private GridPane coursGrid; // GridPane for course cards
    @FXML private ComboBox<String> filterComboBox; // Dropdown for filtering

    private Module currentModule;
    private final CoursService coursService = new CoursService();

    // Filter options
    private static final String FILTER_ALL = "All Courses";
    private static final String FILTER_CREATED_BY_ME = "Créé par moi";
    private static final String FILTER_RECENTLY_ADDED = "Récemment ajouté";

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            moduleTitleLabel.setText("Cours: " + module.getTitle());
            loadCoursCards(); // Load course cards
        }
    }

    @FXML
    public void initialize() {
        // Populate the ComboBox with filter options
        filterComboBox.getItems().addAll(FILTER_ALL, FILTER_CREATED_BY_ME, FILTER_RECENTLY_ADDED);
        filterComboBox.setValue(FILTER_ALL); // Default selection

        // Retrieve the session and check the user's role
        Session session = Session.getInstance();
        String userRole = session.getRole();

        // Hide the filterComboBox if the user's role is ROLE_ETUDIANT
        if ("ROLE_PARENT".equals(userRole)) {
            filterComboBox.setVisible(false); // Hide from UI
            filterComboBox.setManaged(false); // Don't take up space
        } else {
            // Add a listener to handle filter changes for other roles
            filterComboBox.setOnAction(event -> applyFilter());
        }
    }

    private void loadCoursCards() {
        coursGrid.getChildren().clear(); // Clear existing cards

        // Fetch all courses for the current module
        List<Cours> coursList = coursService.getCoursByModule(currentModule.getId());

        // Retrieve the logged-in user's ID
        Session session = Session.getInstance();
        int currentUserId = session.getUserId();

        // Sort the courses: Logged-in user's courses first, followed by others
        coursList.sort((c1, c2) -> {
            boolean isC1CreatedByUser = c1.getUserId() == currentUserId;
            boolean isC2CreatedByUser = c2.getUserId() == currentUserId;

            if (isC1CreatedByUser && !isC2CreatedByUser) {
                return -1; // c1 comes before c2
            } else if (!isC1CreatedByUser && isC2CreatedByUser) {
                return 1; // c2 comes before c1
            } else {
                return 0; // No change in order
            }
        });

        // Debugging logs to confirm the sorting
        System.out.println("Sorted Courses:");
        for (Cours cours : coursList) {
            System.out.println("Course: " + cours.getTitle() + ", Created By: " + cours.getUserId());
        }

        // Add the sorted courses to the GridPane
        loadCoursCardsFiltered(coursList);
    }

    private void loadCoursCardsFiltered(List<Cours> coursList) {
        coursGrid.getChildren().clear(); // Clear existing cards

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
        System.out.println("Selected Filter: " + selectedFilter);

        // Fetch all courses for the current module
        List<Cours> coursList = coursService.getCoursByModule(currentModule.getId());

        // Retrieve the logged-in user's ID
        Session session = Session.getInstance();
        int currentUserId = session.getUserId();

        // Apply the selected filter
        switch (selectedFilter) {
            case FILTER_CREATED_BY_ME:
                coursList.removeIf(c -> c.getUserId() != currentUserId);
                break;

            case FILTER_RECENTLY_ADDED:
                coursList.sort((c1, c2) -> {
                    LocalDateTime updatedAt1 = c1.getUpdatedAt() != null ? c1.getUpdatedAt() : LocalDateTime.MIN;
                    LocalDateTime updatedAt2 = c2.getUpdatedAt() != null ? c2.getUpdatedAt() : LocalDateTime.MIN;
                    return updatedAt2.compareTo(updatedAt1); // Sort descending (newest first)
                });
                break;

            case FILTER_ALL:
            default:
                // No filtering, but prioritize logged-in user's courses
                coursList.sort((c1, c2) -> {
                    boolean isC1CreatedByUser = c1.getUserId() == currentUserId;
                    boolean isC2CreatedByUser = c2.getUserId() == currentUserId;

                    if (isC1CreatedByUser && !isC2CreatedByUser) {
                        return -1; // c1 comes before c2
                    } else if (!isC1CreatedByUser && isC2CreatedByUser) {
                        return 1; // c2 comes before c1
                    } else {
                        return 0; // No change in order
                    }
                });
                break;
        }

        // Debugging logs to confirm the filtering
        System.out.println("Filtered Courses:");
        for (Cours cours : coursList) {
            System.out.println("Course: " + cours.getTitle() + ", Created By: " + cours.getUserId());
        }

        // Reload the course cards with the filtered list
        loadCoursCardsFiltered(coursList);
    }

    private VBox createCoursCard(Cours cours) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefSize(300, 180);

        // ✅ Apply CSS class instead of inline style
        card.getStyleClass().add("module-card");

        // Title (clickable to open PDF)
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
            Stage stage = (Stage) coursGrid.getScene().getWindow(); // Assuming you're using cards now
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

            // Pass the current module and logged-in user ID to the AjoutCoursController
            AjoutCoursController controller = loader.getController();
            controller.setCurrentModule(currentModule);

            // Retrieve the logged-in user's ID directly from the Session entity
            Session session = Session.getInstance();
            int currentUserId = session.getUserId(); // Get the userId from the session
            System.out.println("Retrieved User ID from session: " + currentUserId); // Debugging log
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
            // Get the Dropbox URL for the PDF file
            String dropboxUrl = cours.getPdfName();

            // Check if the URL is valid (basic validation)
            if (dropboxUrl == null || dropboxUrl.isEmpty()) {
                System.err.println("PDF URL is empty or null.");
                return;
            }

            // Dropbox URLs need to be transformed to the correct format for embedding
            // If the URL looks like: https://www.dropbox.com/s/xxx/filename.pdf?dl=0
            // We change it to: https://www.dropbox.com/s/xxx/filename.pdf?raw=1 to open the file directly
            if (dropboxUrl.contains("https://www.dropbox.com/scl/fo/iprdgsydpiq7zr6wicdjm/AK0GS-0W5E1Crxu577QEjpI?rlkey=rdwm7g20fehv9vxcb1cyz9dzz&st=4es8duxl&raw=1")) {
                dropboxUrl = dropboxUrl.replace("?dl=0", "?raw=1");
            }

            // Debugging log to confirm URL formatting
            System.out.println("Opening PDF from URL: " + dropboxUrl);

            // Create a WebView to display the PDF preview from the Dropbox URL
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.load(dropboxUrl);

            // Set up a new stage to show the WebView
            Stage pdfStage = new Stage();
            pdfStage.setTitle("PDF Preview: " + cours.getTitle());
            pdfStage.setScene(new Scene(webView, 800, 600));
            pdfStage.show();

        } catch (Exception e) {
            System.err.println("Error loading PDF preview: " + e.getMessage());
        }
    }
}