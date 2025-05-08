package Controller.Hedy;
import Controller.Hedy.Dahsboard.*;
import entite.Session;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import service.ModuleService;
import entite.Cours;
import entite.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.CoursService;
import service.DropboxService;
import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert.AlertType;


import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class AjoutCoursController {

    @FXML private TextField titleField;
    @FXML private TextField pdfNameField;


    @FXML private Button retourButton;
    @FXML private Button selectPdfButton;
    @FXML private Button saveButton;



    private Module currentModule;
    private final CoursService coursService = new CoursService();
    private Integer currentUserId; // Store the ID of the currently logged-in user
    Session session = Session.getInstance();
    String userRole = session.getRole();
    public void setCurrentModule(Module module) {
        this.currentModule = module;
    }

    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
        System.out.println("Current User ID set to: " + currentUserId); // Debugging log
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            setupButton(retourButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Return to Courses", true);
            setupButton(selectPdfButton, "https://img.icons8.com/?size=100&id=115637&format=png&color=000000", "Select PDF File", true);
            setupButton(saveButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Add Course", true);
        });
    }


    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            // Show text only if showText is true
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new javafx.scene.control.Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60); // Larger width for buttons with text
            // Apply specified style
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new javafx.scene.control.Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            // Apply same style in fallback case
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        }
    }

    @FXML
    private void saveCours() {
        String title = titleField.getText().trim();
        String pdfUrl = pdfNameField.getText().trim();

        if (title.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Le titre ne peut pas être vide.");
            return;
        }

        if (pdfUrl.isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Aucun fichier PDF sélectionné. Veuillez choisir un fichier PDF.");
            return;
        }

        try {
            if (currentUserId == null) {
                showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer l'utilisateur actuel.");
                return;
            }

            Cours newCours = new Cours(title, currentModule, pdfUrl, currentUserId);
            coursService.createPst(newCours);

            ModuleService moduleService = new ModuleService();
            currentModule.setNombreCours(currentModule.getNombreCours() + 1);
            moduleService.update(currentModule);

            showAlert(AlertType.INFORMATION, "Succès", "Le cours a été ajouté avec succès!");

            // 🔁 Determine target FXML based on role
            Session session = Session.getInstance();
            String userRole = session.getRole();

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

            Object controller = loader.getController();
            if (controller instanceof AffichageCoursDashboardHedy) {
                ((AffichageCoursDashboardHedy) controller).setModule(currentModule);
            } else if (controller instanceof AffichageCoursController) {
                ((AffichageCoursController) controller).setModule(currentModule);
            }

            // Wrap content in VBox with header and footer if it's front view
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

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

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            // Load CSS files if needed
            try {
                String storeCardsCss = getClass().getResource("/css/store-cards.css").toExternalForm();
                scene.getStylesheets().add(storeCardsCss);
            } catch (Exception e) {
                System.err.println("Error loading store-cards.css: " + e.getMessage());
            }

            try {
                String navBarCss = getClass().getResource("/navbar.css").toExternalForm();
                scene.getStylesheets().add(navBarCss);
            } catch (Exception e) {
                System.err.println("Error loading navbar.css: " + e.getMessage());
            }

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cours: " + currentModule.getTitle());

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de l'ajout du cours: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
            Parent root = loader.load();

            // Use the correct controller class
            AffichageCoursDashboardHedy controller = loader.getController(); // Correct controller
            controller.setModule(currentModule);

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cours: " + currentModule.getTitle());
        } catch (IOException e) {
            System.err.println("Error loading AffichageCoursDashboard.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void retourCours() {
        try {
            Session session = Session.getInstance();
            String userRole = session.getRole();

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

            // Set module in controller if applicable
            Object controller = loader.getController();
            if (controller instanceof AffichageCoursDashboardHedy) {
                ((AffichageCoursDashboardHedy) controller).setModule(currentModule);
            } else if (controller instanceof AffichageCoursController) {
                ((AffichageCoursController) controller).setModule(currentModule);
            }

            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Only wrap in header/footer if it's front view
            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                // Load and add header
                ImageView headerImageView = loadImageView("/header.png", 1500, 150);
                mainContent.getChildren().add(headerImageView);

                // Style content
                root.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080;");
                mainContent.getChildren().add(root);

                // Load and add footer
                ImageView footerImageView = loadImageView("/footer.png", 1500, 100);
                mainContent.getChildren().add(footerImageView);
            } else {
                mainContent.getChildren().add(root); // Admin doesn't need header/footer
            }

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            // Add CSS if available
            addStylesheet(scene, "/css/store-cards.css");
            addStylesheet(scene, "/navbar.css");

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cours: " + currentModule.getTitle());

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger la page demandée.");
        }
    }

    // Helper to load images safely
    private ImageView loadImageView(String imagePath, double fitWidth, double height) {
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(getClass().getResource(imagePath).toExternalForm());
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(fitWidth);
            imageView.setSmooth(true);
            imageView.setCache(true);
        } catch (Exception e) {
            Rectangle fallback = new Rectangle(1000, height);
            fallback.setFill(Color.LIGHTGRAY);
            Label errorLabel = new Label("Image not found: " + imagePath);
            errorLabel.setStyle("-fx-font-size: 14; -fx-text-fill: red;");
            VBox fallbackBox = new VBox(errorLabel, fallback);
            VBox.setMargin(fallbackBox, new Insets(10));
            VBox container = new VBox(fallbackBox);
            container.setAlignment(Pos.CENTER);
            ((Pane) imageView.getParent()).getChildren().add(container);
            return null;
        }
        return imageView;
    }

    // Helper to add stylesheets
    private void addStylesheet(Scene scene, String cssPath) {
        try {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + e.getMessage());
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
                String dropboxAccessToken = "sl.u.AFuC7lPdssE1XoZRpfrqND2gsMRCTSh8WmMIMXGzZdEBucYnNYHm6_urgX3uNIbiD0emrJ5tW2rmJXuv5P9CnGk9RkdqRYQfK_XKphIQ091oykxiDj0JdemLCxXTKMD4gImClxgPiYHdzR4Doej0ESOWndv82C16jisMmVrtKwNFgHQWZx8g28NftudxLATymmkiDusRintaIzHZD_WLE4S2dtcEeu9Lmzt8vcamHXfYY0zRTvrJmc-Hs36FOumVxtVlUAQTQ3pZm6aU3Y79rn2C6Aak8erlz6Zfq24hl6YwNfNKY_BV8-I5oC3dbQCW1wXGCzOeiio5JBoS_Msd-We9W_qWhm4GU6t7O459lVkHuPATsI5uSovo0dkhbQ4Rnf3lV0JpgtrvNFUlpvnHC9EBSn7EGxagHsbzjzuMCGHE56QUp4STJX_jgDeDKb9CwOVrW0DCofLFD4H1JhV2j8fDkAP8F-WcxpyDYdOydawplD6J1Ggdg97fgI4bugtKCsV6jWi8A1lYh05eUrZh-X66Yfu2tXC-r81Qn-HFsNbo6AQtnzkFcv_SO0WIyUfQSkIucFhVDoO00ZolZG0jNtvbd2xY62d_anH7FeSHeXk_9q4uRBRo44BmJU-Rt9ZJKUT4BZTaOPeO_bBlakdR2zU4j28WTpNp_Gx6CIZCYE2cK4oe8BtTNbiiJMUH8LNenOouZ2ysadpo1rVx3lcQyscxsHkobdP3lKXcMA2mVHqNmjMaYPSBr6F5saFnW9v2j7Cqwj-R3TtXKYy8Isc0fGFLvIg_Y9TR_7ECo4HBT5xjKnH3B-dzWM6taeoxglGf0moWmHfs6tNC8KuNJHHm5gt5I26yRqLG5Sy72od9CAPrw9tKeBQP-f4WGU_jjFdmCrkgO4TEUELRb8bDIrLdtIFqauCk7YfpVJfJK3tOIqbbCFJl-5sELL2crt56NZV4HzXeaNt1rNCmdkOGBW6rAIiAQjLxDa83xLZd52vNKdZKWwIeFZJi6VxlgNB80-FTmcYlvsgAq7D_5h73DWhO30aEB5ap338CdplkLzZNt0jQsN5u1c988VGTH0Pp44Q9n9zphsEZ_YEKZka-x1WgnWlt26t1CE3Ud11JUSvIrALpRLzChgHiD0Js4T07xdH6F3uJo4XdPumgcBm2olvsrO8OD7VS8eUivJVZ67YmfSWwzPKKPCetH_Mlp_hloDmOL88wphR73QbsXnex8KIsAJqs_1Vw8azezYIAbq42_PAXUNbK2DmqLGbhkjacpEK6oqerM11N1BA4Aap7yPUz99hx"; // Replace with your Dropbox access token
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

    // Helper method to show alerts
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait(); // Show the alert and wait for user response
    }
}