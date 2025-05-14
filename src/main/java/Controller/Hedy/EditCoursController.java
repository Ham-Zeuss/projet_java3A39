package Controller.Hedy;

import Controller.Hedy.Dahsboard.AffichageCoursDashboardHedy;
import entite.Cours;
import entite.Module;
import entite.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane; // Added missing import
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import service.CoursService;
import service.DropboxService;
import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import service.ModuleService;

public class EditCoursController {

    @FXML private TextField titleField;
    @FXML private TextField pdfNameField;

    private Cours coursToEdit;
    private final CoursService coursService = new CoursService();
    private Session session = Session.getInstance();
    private String userRole = session.getRole();

    public void setCoursToEdit(Cours cours) {
        this.coursToEdit = cours;

        // Pre-fill the fields with the existing course data
        if (cours != null) {
            titleField.setText(cours.getTitle());
            pdfNameField.setText(cours.getPdfName());
        }
    }

    @FXML
    private void saveCours() {
        // Validate input fields
        String title = titleField.getText().trim();
        String pdfName = pdfNameField.getText().trim();

        if (title.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
            return;
        }

        if (pdfName.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Aucun fichier PDF sélectionné. Veuillez choisir un fichier PDF.");
            return;
        }

        try {
            // Update the course object
            coursToEdit.setTitle(title);
            coursToEdit.setPdfName(pdfName); // Update the PDF file URL
            coursToEdit.setUpdatedAt(java.time.LocalDateTime.now());

            // Save the updated course to the database
            coursService.update(coursToEdit);

            // Show success message
            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été modifié avec succès!");

            // Determine target FXML based on role
            String fxmlPath;
            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                fxmlPath = "/HedyFXML/AffichageCoursFront.fxml";
            } else if ("ROLE_ADMIN".equals(userRole)) {
                fxmlPath = "/HedyFXML/AffichageCoursDashboard.fxml";
            } else {
                showAlert(AlertType.ERROR, "Accès refusé", "Vous n'avez pas les droits nécessaires pour accéder à cette page.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Set module in the appropriate controller
            Object controller = loader.getController();
            if (controller instanceof AffichageCoursDashboardHedy) {
                ((AffichageCoursDashboardHedy) controller).setModule(coursToEdit.getModuleId());
            } else if (controller instanceof AffichageCoursController) {
                ((AffichageCoursController) controller).setModule(coursToEdit.getModuleId());
            }

            // Wrap content in VBox with header and footer for front-end view
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                // Load header image
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResource("/header.png").toExternalForm());
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(1500);
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(1000, 150);
                    fallbackHeader.setFill(Color.LIGHTGRAY);
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }

                // Add header
                mainContent.getChildren().add(headerImageView);

                // Style the loaded FXML content
                root.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
                mainContent.getChildren().add(root);

                // Load footer image
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResource("/footer.png").toExternalForm());
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(1500);
                } catch (Exception e) {
                    System.err.println("Error loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(1000, 100);
                    fallbackFooter.setFill(Color.LIGHTGRAY);
                    Label errorLabel = new Label("Footer image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                    mainContent.getChildren().add(fallbackBox);
                }

                // Add footer
                mainContent.getChildren().add(footerImageView);
            } else {
                mainContent.getChildren().add(root); // Admin view doesn't need header/footer
            }

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            // Load CSS files (excluding navbar.css for front-end)
            try {
                String storeCardsCss = getClass().getResource("/css/store-cards.css").toExternalForm();
                scene.getStylesheets().add(storeCardsCss);
            } catch (Exception e) {
                System.err.println("Error loading store-cards.css: " + e.getMessage());
            }

            // Only load navbar.css for admin view
            if ("ROLE_ADMIN".equals(userRole)) {
                try {
                    String navBarCss = getClass().getResource("/navbar.css").toExternalForm();
                    scene.getStylesheets().add(navBarCss);
                } catch (Exception e) {
                    System.err.println("Error loading navbar.css: " + e.getMessage());
                }
            }

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cours: " + coursToEdit.getModuleId().getTitle());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de la modification du cours: " + e.getMessage());
        }
    }

    // Helper method to show alerts
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait(); // Show the alert and wait for user response
    }

    @FXML
    private void cancel() {
        try {
            // Determine target FXML based on role
            String fxmlPath;
            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                fxmlPath = "/HedyFXML/AffichageCoursFront.fxml";
            } else if ("ROLE_ADMIN".equals(userRole)) {
                fxmlPath = "/HedyFXML/AffichageCoursDashboard.fxml";
            } else {
                showAlert(AlertType.ERROR, "Accès refusé", "Vous n'avez pas les droits nécessaires pour accéder à cette page.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Set module in the appropriate controller
            Object controller = loader.getController();
            if (controller instanceof AffichageCoursDashboardHedy) {
                ((AffichageCoursDashboardHedy) controller).setModule(coursToEdit.getModuleId());
            } else if (controller instanceof AffichageCoursController) {
                ((AffichageCoursController) controller).setModule(coursToEdit.getModuleId());
            }

            // Wrap content in VBox with header and footer for front-end view
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                // Load header image
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResource("/header.png").toExternalForm());
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(1500);
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(1000, 150);
                    fallbackHeader.setFill(Color.LIGHTGRAY);
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }

                // Add header
                mainContent.getChildren().add(headerImageView);

                // Style the loaded FXML content
                root.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080;");
                mainContent.getChildren().add(root);

                // Load footer image
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResource("/footer.png").toExternalForm());
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(1500);
                } catch (Exception e) {
                    System.err.println("Error loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(1000, 100);
                    fallbackFooter.setFill(Color.LIGHTGRAY);
                    Label errorLabel = new Label("Footer image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                    mainContent.getChildren().add(fallbackBox);
                }

                // Add footer
                mainContent.getChildren().add(footerImageView);
            } else {
                mainContent.getChildren().add(root); // Admin view doesn't need header/footer
            }

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            // Load CSS files (excluding navbar.css for front-end)
            try {
                String storeCardsCss = getClass().getResource("/css/store-cards.css").toExternalForm();
                scene.getStylesheets().add(storeCardsCss);
            } catch (Exception e) {
                System.err.println("Error loading store-cards.css: " + e.getMessage());
            }

            // Only load navbar.css for admin view
            if ("ROLE_ADMIN".equals(userRole)) {
                try {
                    String navBarCss = getClass().getResource("/navbar.css").toExternalForm();
                    scene.getStylesheets().add(navBarCss);
                } catch (Exception e) {
                    System.err.println("Error loading navbar.css: " + e.getMessage());
                }
            }

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cours: " + coursToEdit.getModuleId().getTitle());
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
        }
    }

    @FXML
    private void selectPdfFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        Stage stage = (Stage) titleField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Initialize Dropbox Service
                String dropboxAccessToken ="sl.u.AFvzlkyJmNYyuxrPkvREKtuYgC6_SRf4BqbeHNIPQ_-xg6zEMRdrpSGsGayUCM-qoXRwguxGq0GEY5FZs3ILur4_i47vLaKTwCY8pqkjwOvDES91OF3kNYtF7cFkNXpnwMNXGUhhuEBKHEcXeC3_t5zdU3DB_e7RTP7FUKIVmmWbI-PbyTx8LH-Ki0fxrxg6LIsb-Zdc2YegbDbKvb5Eo6uUTtcQ4nhz-yzKnwOHftDpLOhy0gA_QlpVTfCc3CR1q5THv6PGcyReBbLNDHx-lJILl8XG86fht_JxovUcHdNJuzAXrDZ5xTiJVuoy2S5OhUU9_dBiYCTj_-dA9xnXZj4AP2Gg4W0aNHnB8dO_WYN9EBNKLeAjUEs9hYmG-qcEV4Zj-ANq8iTQLBfbM4GDC5cHKEtxEoC7RUISExBnTAqOjphduLtiyCmLUv89TnIOveV-ZYPMkrV54mkHX_yXuuXjGIJIOJiosARscOr54O5excLQOefMGKAozvuURFXoWMcbLtoocqRbElmRNB5PL9Ntp2uFzzEYf-NcUjOXTGCotA4StWcGW4u5o_n0IJfOwCcLs_2uEUq0Q3A48q7KycRncW_VlcASy0Qi7TQwm9W3s653WCoCvfuoNxanwwQej_5IEWYF-0W5pwQhPvVPF4EqqVqHNcURNNUxwwvkQlSe9cTvCoM0g_MJbIgP0Nx94My7ocZLVuQDWoJcKDj0R-EJlfhepatIeYL28mEqM9_2MGUcOPTaVw98PeAG3OEQzJi_UsnyAlSnydUZ_txqLxXJIg2pxl0O1ZjVHyhcMxuuUjZGL8O6bU0ZKRNqNz42VXQmXzVMp2VLFgUY6vxCj96ryc34yOu6sg2JQHdiC2l4yDOFkjX9UfO9cqCis48rY4hnyftJkhOeOpQ7pUOWK2LwvZOhTtgAngYxFwkg1belXtIhEvPQJko9PnBE7KmwKLgRvgQQ_SSUfUl6SB-hdNW58wHWE3RWayufYCjssvm4xWhUgivyBJCZJWeBN5o0JeUEiOlhGEis0F_9r6EWv92tVWsZLbp4jPEbMjkOwdzJS98lG8p9BYewWqE5G2mn_-x72JUr4kRgRxCW4fKfApehkFdKfcmueN0toaiVB0yhWE8xkxgGneVAqsBaWqWU6mj3pXZS86JvyHW1LnMEozxJMwBt6YfVk0RBkhdJaG3nCke8TgVPi5jxTryQ8Q3XZgiKNVoheF6529YOhyfmGJSNT3cc9aUhCw49DY57ttGGVUvXN5F9vkDCwX1dPCUl5Y2qlRyyGC_nCf7xYlTYTRsa";
                DropboxService dropboxService = new DropboxService(dropboxAccessToken);

                // Define the destination path in Dropbox
                String dropboxPath = "/pdfs/" + selectedFile.getName(); // Destination path in Dropbox

                // Upload the selected PDF file to Dropbox
                dropboxService.uploadFile(selectedFile.getAbsolutePath(), dropboxPath);

                // Retrieve the public URL of the uploaded file
                String fileUrl = dropboxService.getFileUrl(dropboxPath);

                if (fileUrl == null) {
                    showAlert(AlertType.ERROR, "Erreur d'upload", "Impossible de récupérer l'URL du fichier.");
                    return;
                }

                // Update the pdfNameField with the public URL of the uploaded file
                pdfNameField.setText(fileUrl);

                // Show success message
                showAlert(AlertType.INFORMATION, "Succès", "Le fichier PDF a été uploadé avec succès!");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur d'upload", "Une erreur s'est produite lors de l'upload du fichier PDF: " + e.getMessage());
            }
        }
    }

    public static class AffichageModuleController {

        @FXML private GridPane modulesGrid;
        private final ModuleService moduleService = new ModuleService();

        @FXML
        public void initialize() {
            // Spacing & Padding setup
            modulesGrid.setVgap(15); // Vertical spacing between cards
            modulesGrid.setHgap(0);  // No horizontal spacing
            modulesGrid.setPadding(new Insets(20)); // Padding around grid

            loadModuleCards();
        }

        private void loadModuleCards() {
            modulesGrid.getChildren().clear(); // Clear existing cards
            List<entite.Module> modules = moduleService.readAll();

            int columns = 1; // Only one card per row
            int row = 0;
            int column = 0;

            for (entite.Module module : modules) {
                VBox card = createModuleCard(module);
                GridPane.setMargin(card, new Insets(10, 30, 10, 100)); // top, right, bottom, left
                modulesGrid.add(card, column, row);
                row++; // Move to next row
            }
        }

        private void showModuleCourses(entite.Module module) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
                Parent root = loader.load();

                AffichageCoursController controller = loader.getController();
                controller.setModule(module);

                Stage stage = (Stage) modulesGrid.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Cours: " + module.getTitle());
            } catch (IOException e) {
                System.err.println("Error loading AffichageCoursDashboard.fxml: " + e.getMessage());
            }
        }

        private VBox createModuleCard(Module module) {
            VBox card = new VBox(10);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new Insets(15));
            card.setPrefSize(600, 180);
            card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);
                -fx-cursor: hand;
            """);

            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle("""
                -fx-background-color: #f9f9f9;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);
                -fx-cursor: hand;
            """));
            card.setOnMouseExited(e -> card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);
                -fx-cursor: hand;
            """));

            // Click action
            card.setOnMouseClicked(e -> showModuleCourses(module));

            // Title
            Label titleLabel = new Label(module.getTitle());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            titleLabel.setStyle("-fx-text-fill: #2c3e50;");

            // Description
            Label descLabel = new Label(module.getDescription());
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: #7f8c8d;");

            // Details
            HBox detailsBox = new HBox(20);
            detailsBox.setAlignment(Pos.CENTER_LEFT);
            Label countLabel = new Label(module.getNombreCours() + " cours");
            countLabel.setStyle("-fx-text-fill: #2980b9;");
            Label levelLabel = new Label("Niveau: " + module.getLevel());
            levelLabel.setStyle("-fx-text-fill: #27ae60;");
            detailsBox.getChildren().addAll(countLabel, levelLabel);

            // Assemble
            card.getChildren().addAll(titleLabel, descLabel, detailsBox);
            return card;
        }

        @FXML
        private void goToAjoutPage() {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/HedyFXML/AjoutModule.fxml"));
                Stage stage = (Stage) modulesGrid.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Ajouter Module");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}