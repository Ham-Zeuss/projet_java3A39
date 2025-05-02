package Controller.Hedy.Dahsboard;
import Controller.Hedy.*;
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
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CoursService;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AffichageCoursDashboardHedy{

    @FXML private Label moduleTitleLabel;
    @FXML private GridPane coursGrid; // GridPane for course cards
    @FXML
    private Label courseCountLabel;

    private Module currentModule;
    private final CoursService coursService = new CoursService();

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            moduleTitleLabel.setText("Module: " + module.getTitle());
            loadCoursCards(); // Load course cards
        }
    }

    public void loadCoursCards() {
        coursGrid.getChildren().clear();  // Clear previous courses

        List<Cours> coursList = coursService.getCoursByModule(currentModule.getId());

        // ✅ Update course count label safely
        if (courseCountLabel != null) {
            courseCountLabel.setText("Nombre de cours : " + coursList.size());
        }

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


    // Create the course card with buttons (Edit and Delete)
    private VBox createCoursCard(Cours cours) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 5, 0, 0);");
        card.setPrefSize(300, 180);
        card.setOnMouseClicked(event -> {
            openPdf(cours); // Handle PDF open
        });

        // Title
        Label titleLabel = new Label(cours.getTitle());
        titleLabel.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // PDF Name
        Label pdfLabel = new Label("PDF: " + cours.getPdfName());
        pdfLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Updated At
        String updatedAtText = (cours.getUpdatedAt() != null)
                ? "Dernière modification: " + cours.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Dernière modification: N/A";
        Label updatedAtLabel = new Label(updatedAtText);
        updatedAtLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Buttons
        HBox buttonBox = new HBox(10);
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        editButton.setOnAction(e -> editCours(cours));
        deleteButton.setOnAction(e -> {
            // Delete the course
            coursService.delete(cours);
            // After deleting, reload the course cards and update the count label
            loadCoursCards();
        });

        buttonBox.getChildren().addAll(editButton, deleteButton);

        // Add all components to card
        card.getChildren().addAll(titleLabel, pdfLabel, updatedAtLabel, buttonBox);

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
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            javafx.scene.web.WebEngine webEngine = webView.getEngine();
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