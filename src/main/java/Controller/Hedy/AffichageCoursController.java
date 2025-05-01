package Controller.Hedy;

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

public class AffichageCoursController {

    @FXML private Label moduleTitleLabel;
    @FXML private GridPane coursGrid; // GridPane for course cards

    private Module currentModule;
    private final CoursService coursService = new CoursService();

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            moduleTitleLabel.setText("Cours: " + module.getTitle());
            loadCoursCards(); // Load course cards
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

    private VBox createCoursCard(Cours cours) {
        // Create the card layout
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 5, 0, 0);");
        card.setPrefSize(300, 180);
        card.setOnMouseClicked(event -> {
            openPdf(cours);
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

        // Retrieve the logged-in user's ID and role
        Session session = Session.getInstance();
        int currentUserId = session.getUserId(); // Get the logged-in user's ID
        String userRole = session.getRole(); // Get the logged-in user's role

        // Debugging logs
        System.out.println("Logged-in User ID: " + currentUserId);
        System.out.println("Course: " + cours.getTitle() + ", Created By: " + cours.getUserId());

        // Check if the course was created by the logged-in user
        boolean isCourseCreatedByUser = cours.getUserId() == currentUserId;

        System.out.println("Is course created by logged-in user? " + isCourseCreatedByUser);

        // Show buttons only for the course creator
        if (isCourseCreatedByUser) {
            System.out.println("Adding buttons for course: " + cours.getTitle());
            buttonBox.getChildren().addAll(editButton, deleteButton);
        } else {
            System.out.println("Hiding buttons for course: " + cours.getTitle());
            buttonBox.setVisible(false);
            buttonBox.setManaged(false); // Ensures it doesn't take up space in the layout
        }

        // Add actions to the buttons
        editButton.setOnAction(e -> editCours(cours));
        deleteButton.setOnAction(e -> {
            coursService.delete(cours);
            loadCoursCards(); // Refresh after delete
        });

        // Add all components to the card
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
            // Resolve the full file path using the stored file name
            File pdfFile = new File("resources/pdf/" + cours.getPdfName());

            if (!pdfFile.exists()) {
                System.err.println("PDF file not found: " + pdfFile.getAbsolutePath());
                return;
            }

            // Open the PDF file in the default viewer
            Desktop.getDesktop().open(pdfFile);
        } catch (Exception e) {
            System.err.println("Error opening PDF: " + e.getMessage());
        }
    }
}