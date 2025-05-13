package Controller.Hedy;
import Controller.Hedy.Dahsboard.*;
import entite.Session;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
import java.net.URL;

import javafx.stage.FileChooser;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Screen;

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
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new javafx.scene.control.Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            button.setText(tooltipText);
            button.setTooltip(new javafx.scene.control.Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
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

            // Determine target FXML based on role
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

            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AffichageCoursDashboardHedy) {
                ((AffichageCoursDashboardHedy) controller).setModule(currentModule);
            } else if (controller instanceof AffichageCoursController) {
                ((AffichageCoursController) controller).setModule(currentModule);
            }

            // Wrap content in VBox with header and footer for front view
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                // Load header FXML (navbar)
                FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                VBox headerFxmlContent = headerLoader.load();
                headerFxmlContent.setPrefSize(width * 0.6, 100);
                mainContent.getChildren().add(headerFxmlContent);

                // Load header image
                ImageView headerImageView = loadImageView("/header.png", width, 150);
                if (headerImageView != null) {
                    mainContent.getChildren().add(headerImageView);
                }

                // Style the loaded FXML content
                root.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
                root.getStyleClass().add("body-content");
                mainContent.getChildren().add(root);

                // Load footer image
                ImageView footerImageView = loadImageView("/footer.png", width, 100);
                if (footerImageView != null) {
                    mainContent.getChildren().add(footerImageView);
                }
            } else {
                // Admin view: no header/footer
                root.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
                mainContent.getChildren().add(root);
            }

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, width, height);

            // Load CSS files
            addStylesheet(scene, "/css/store-cards.css");
            addStylesheet(scene, "/navbar.css");
            addStylesheet(scene, "/css/affichageprofilefront.css");
            addStylesheet(scene, "/css/appointments.css");
            addStylesheet(scene, "/css/GooButton.css");
            addStylesheet(scene, "/css/GamesMenuStyling.css");

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cours: " + currentModule.getTitle());
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite lors de l'ajout du cours: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        try {
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageCoursDashboard.fxml"));
            Parent root = loader.load();

            AffichageCoursDashboardHedy controller = loader.getController();
            controller.setModule(currentModule);

            Scene scene = new Scene(root, width, height);
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cours: " + currentModule.getTitle());
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading AffichageCoursDashboard.fxml: " + e.getMessage());
            showAlert(AlertType.ERROR, "Erreur de chargement", "Impossible de charger la page demandée: " + e.getMessage());
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

            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AffichageCoursDashboardHedy) {
                ((AffichageCoursDashboardHedy) controller).setModule(currentModule);
            } else if (controller instanceof AffichageCoursController) {
                ((AffichageCoursController) controller).setModule(currentModule);
            }

            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            if ("ROLE_ENSEIGNANT".equals(userRole)) {
                // Load header FXML (navbar)
                FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                VBox headerFxmlContent = headerLoader.load();
                headerFxmlContent.setPrefSize(width * 0.6, 100);
                mainContent.getChildren().add(headerFxmlContent);

                // Load header image
                ImageView headerImageView = loadImageView("/header.png", width, 150);
                if (headerImageView != null) {
                    mainContent.getChildren().add(headerImageView);
                }

                // Style content
                root.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
                root.getStyleClass().add("body-content");
                mainContent.getChildren().add(root);

                // Load footer image
                ImageView footerImageView = loadImageView("/footer.png", width, 100);
                if (footerImageView != null) {
                    mainContent.getChildren().add(footerImageView);
                }
            } else {
                // Admin view: no header/footer
                root.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
                mainContent.getChildren().add(root);
            }

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            addStylesheet(scene, "/css/store-cards.css");
            addStylesheet(scene, "/navbar.css");
            addStylesheet(scene, "/css/affichageprofilefront.css");
            addStylesheet(scene, "/css/appointments.css");
            addStylesheet(scene, "/css/GooButton.css");
            addStylesheet(scene, "/css/GamesMenuStyling.css");

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cours: " + currentModule.getTitle());
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            showAlert(AlertType.ERROR, "Erreur de chargement", "Impossible de charger la page demandée: " + e.getMessage());
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

    private void addStylesheet(Scene scene, String cssPath) {
        try {
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS file not found: " + cssPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + cssPath + " - " + e.getMessage());
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
                String dropboxAccessToken = "sl.u.AFuC7lPdssE1XoZRpfrqND2gsMRCTSh8WmMIMXGzZdEBucYnNYHm6_urgX3uNIbiD0emrJ5tW2rmJXuv5P9CnGk9RkdqRYQfK_XKphIQ091oykxiDj0JdemLCxXTKMD4gImClxgPiYHdzR4Doej0ESOWndv82C16jisMmVrtKwNFgHQWZx8g28NftudxLATymmkiDusRintaIzHZD_WLE4S2dtcEeu9Lmzt8vcamHXfYY0zRTvrJmc-Hs36FOumVxtVlUAQTQ3pZm6aU3Y79rn2C6Aak8erlz6Zfq24hl6YwNfNKY_BV8-I5oC3dbQCW1wXGCzOeiio5JBoS_Msd-We9W_qWhm4GU6t7O459lVkHuPATsI5uSovo0dkhbQ4Rnf3lV0JpgtrvNFUlpvnHC9EBSn7EGxagHsbzjzuMCGHE56QUp4STJX_jgDeDKb9CwOVrW0DCofLFD4H1JhV2j8fDkAP8F-WcxpyDYdOydawplD6J1Ggdg97fgI4bugtKCsV6jWi8A1lYh05eUrZh-X66Yfu2tXC-r81Qn-HFsNbo6AQtnzkFcv_SO0WIyUfQSkIucFhVDoO00ZolZG0jNtvbd2xY62d_anH7FeSHeXk_9q4uRBRo44BmJU-Rt9ZJKUT4BZTaOPeO_bBlakdR2zU4j28WTpNp_Gx6CIZCYE2cK4oe8BtTNbiiJMUH8LNenOouZ2ysadpo1rVx3lcQyscxsHkobdP3lKXcMA2mVHqNmjMaYPSBr6F5saFnW9v2j7Cqwj-R3TtXKYy8Isc0fGFLvIg_Y9TR_7ECo4HBT5xjKnH3B-dzWM6taeoxglGf0moWmHfs6tNC8KuNJHHm5gt5I26yRqLG5Sy72od9CAPrw9tKeBQP-f4WGU_jjFdmCrkgO4TEUELRb8bDIrLdtIFqauCk7YfpVJfJK3tOIqbbCFJl-5sELL2crt56NZV4HzXeaNt1rNCmdkOGBW6rAIiAQds52vNKdZKWwIeFZJi6VxlgNB80-FTmcYlvsgAq7D_5h73DWhO30aEB5ap338CdplkLzZNt0jQsN5u1c988VGTH0Pp44Q9n9zphsEZ_YEKZka-x1WgnWlt26t1CE3Ud11JUSvIrALpRLzChgHiD0Js4T07xdH6F3uJo4XdPumgcBm2olvsrO8OD7VS8eUivJVZ67YmfSWwzPKKPCetH_Mlp_hloDmOL88wphR73QbsXnex8KIsAJqs_1Vw8azezYIAbq42_PAXUNbK2DmqLGbhkjacpEK6oqerM11N1BA4Aap7yPUz99hx"; // Replace with your Dropbox access token
                DropboxService dropboxService = new DropboxService(dropboxAccessToken);

                String dropboxPath = "/pdfs/" + selectedFile.getName();
                dropboxService.uploadFile(selectedFile.getAbsolutePath(), dropboxPath);
                String fileUrl = dropboxService.getFileUrl(dropboxPath);

                if (fileUrl == null) {
                    showAlert(AlertType.ERROR, "Erreur d'upload", "Impossible de récupérer l'URL du fichier.");
                    return;
                }

                pdfNameField.setText(fileUrl);
                showAlert(AlertType.INFORMATION, "Succès", "Le fichier PDF a été uploadé avec succès!");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur d'upload", "Une erreur s'est produite lors de l'upload du fichier PDF: " + e.getMessage());
            }
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}