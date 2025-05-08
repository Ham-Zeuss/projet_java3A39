package Controller.Hedy;

import Controller.Oumaima.affichageEtudiantQuiz;
import Controller.Oumaima.affichageQuizcontroller;
import entite.Rating;
import entite.Cours;
import entite.Module;
import entite.Session;
import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.CoursService;
import service.Oumaima.QuizService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AffichageCoursController {

    @FXML
    private Label moduleTitleLabel;
    @FXML
    private GridPane coursGrid;
    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button ajouterCoursButton;

    @FXML
    private Button retourModulesButton;

    private Module currentModule;
    private final CoursService coursService = new CoursService();

    private static final String FILTER_ALL = "All Courses";
    private static final String FILTER_CREATED_BY_ME = "Créé par moi";
    private static final String FILTER_RECENTLY_ADDED = "Récemment ajouté";

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            moduleTitleLabel.setText("Module: " + module.getTitle());
            loadCoursCards();
        }

        Session session = Session.getInstance();
        String userRole = session.getRole();

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

        // Configure buttons with icons and text
        setupButton(ajouterCoursButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Ajouter Cours", true);
        setupButton(retourModulesButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Return", true);
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            // Show text only if showText is true
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60); // Larger width for buttons with text
            // Apply specified style
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-graphic-text-gap: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            // Apply same style in fallback case
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: black; -fx-border-color: transparent;");
            button.getStyleClass().add("icon-button");
        }
    }

    private void loadCoursCards() {
        coursGrid.getChildren().clear();
        List<Cours> coursList = coursService.getCoursByModule(currentModule.getId());

        Session session = Session.getInstance();
        int currentUserId = session.getUserId();

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
                    return updatedAt2.compareTo(updatedAt1);
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

        card.getStyleClass().add("module-card");

        Label titleLabel = new Label(cours.getTitle());
        titleLabel.getStyleClass().add("heading");
        titleLabel.setOnMouseClicked(event -> openPdf(cours));

        Label pdfLabel = new Label("PDF: " + cours.getPdfName());
        pdfLabel.getStyleClass().add("para");

        String updatedAtText = (cours.getUpdatedAt() != null)
                ? "Dernière modification: " + cours.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Dernière modification: N/A";
        Label updatedAtLabel = new Label(updatedAtText);
        updatedAtLabel.getStyleClass().add("para");

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

            boolean alreadyRated = existingRating > 0;

            for (int i = 0; i < 5; i++) {
                Label star = new Label(i < existingRating ? "★" : "☆");
                star.setStyle("-fx-font-size: 20px; -fx-text-fill: gold;");
                final int ratingValue = i + 1;

                if (alreadyRated) {
                    star.setDisable(true); // Disable interaction
                    star.setCursor(Cursor.DEFAULT);
                } else {
                    star.setOnMouseClicked(event -> {
                        event.consume();

                        boolean success = RatingsStorage.addRatingIfNotExists(cours.getId(), currentUserId, ratingValue);

                        if (!success) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Avis déjà soumis");
                            alert.setHeaderText(null);
                            alert.setContentText("Vous avez déjà noté ce cours. Vous ne pouvez pas voter à nouveau.");
                            alert.showAndWait();
                            return;
                        }

                        // Update UI after successful rating
                        for (int j = 0; j < 5; j++) {
                            starLabels[j].setText(j < ratingValue ? "★" : "☆");
                        }

                        // Optionally disable stars after rating
                        for (Label s : starLabels) {
                            s.setDisable(true);
                            s.setCursor(Cursor.DEFAULT);
                        }
                    });
                }

                starLabels[i] = star;
                starsBox.getChildren().add(star);
            }
        }

        HBox buttonBox = new HBox(10);
        Button editButton = new Button();
        Button deleteButton = new Button();
        Button viewQuizButton = new Button();

        // Configure buttons with icons and text
        setupButton(viewQuizButton, "https://img.icons8.com/?size=100&id=114917&format=png&color=000000", "Voir", false);
        setupButton(editButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Modifier", false);
        setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Supprimer", false);

        if ("ROLE_PARENT".equals(userRole) || "ROLE_ENSEIGNANT".equals(userRole)) {
            buttonBox.getChildren().add(viewQuizButton);
        }

        boolean isCourseCreatedByUser = cours.getUserId() == currentUserId;
        if (isCourseCreatedByUser) {
            buttonBox.getChildren().addAll(editButton, deleteButton);
        }

        buttonBox.setVisible(!buttonBox.getChildren().isEmpty());
        buttonBox.setManaged(!buttonBox.getChildren().isEmpty());

        editButton.setOnAction(e -> editCours(cours));
        deleteButton.setOnAction(e -> {
            coursService.delete(cours);
            loadCoursCards();
        });

        viewQuizButton.setOnAction(e -> {
            if ("ROLE_PARENT".equals(userRole)) {
                goToQuizList2(cours);
            } else if ("ROLE_ENSEIGNANT".equals(userRole)) {
                goToQuizList(cours);
            }
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
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // 1. Load header.fxml (contains navbar)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // 2. Add header image (banner under navbar)
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1400);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // 3. Load body content (module list)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/HedyFXML/AffichageModule.fxml"));
            Parent bodyContent = bodyLoader.load();
            mainContent.getChildren().add(bodyContent);

            // 4. Load footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1400);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Create scene
            Scene scene = new Scene(scrollPane, 1400, 800);

            // Load CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());

            URL navBarCss = getClass().getResource("/navbar.css");
            if (navBarCss != null) scene.getStylesheets().add(navBarCss.toExternalForm());

            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());

            URL appointmentsCss = getClass().getResource("/css/appointments.css");
            if (appointmentsCss != null) scene.getStylesheets().add(appointmentsCss.toExternalForm());

            URL gooButtonCss = getClass().getResource("/css/GooButton.css");
            if (gooButtonCss != null) scene.getStylesheets().add(gooButtonCss.toExternalForm());

            URL gamesMenuStylingCss = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStylingCss != null) scene.getStylesheets().add(gamesMenuStylingCss.toExternalForm());

            // Set scene on stage
            Stage stage = (Stage) coursGrid.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Liste des Modules");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading module page: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des modules: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void goToAjoutCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HedyFXML/AjoutCours" +
                    ".fxml"));
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

    private void goToQuizList(Cours cours) {
        try {
            if (cours == null || cours.getId() <= 0) {
                throw new IllegalArgumentException("Cours invalide");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
            Parent root = loader.load();
            affichageQuizcontroller controller = loader.getController();

            List<Quiz> filteredQuizzes = controller.getQuizService().readAll().stream()
                    .filter(quiz -> quiz.getCourse() != null && quiz.getCourse().getId() == cours.getId())
                    .collect(Collectors.toList());
            controller.displayQuizzes1(filteredQuizzes);

            Stage stage = (Stage) coursGrid.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

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
                VBox fallbackBox = new VBox();
                fallbackBox.getChildren().addAll(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            root.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(root);

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
                VBox fallbackBox = new VBox();
                fallbackBox.getChildren().addAll(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            double headerHeight = headerImageView.getImage() != null ? headerImageView.getImage().getHeight() : 150;
            double footerHeight = footerImageView.getImage() != null ? footerImageView.getImage().getHeight() : 100;
            double totalHeight = headerHeight + root.prefHeight(-1) + footerHeight;

            Scene scene = new Scene(scrollPane, 1500, 700);

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

            stage.setScene(scene);
            stage.setTitle("Quizzes du Cours: " + cours.getTitle());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void goToQuizList2(Cours cours) {
        try {
            if (cours == null || cours.getId() <= 0) {
                throw new IllegalArgumentException("Cours invalide");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageEtudiantQuiz.fxml"));
            Parent root = loader.load();

            affichageEtudiantQuiz controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller is null for affichageEtudiantQuiz.fxml");
            }

            QuizService quizService = new QuizService();
            List<Quiz> allQuizzes = quizService.readAll();
            List<Quiz> filteredQuizzes = allQuizzes.stream()
                    .filter(quiz -> quiz.getCourse() != null && quiz.getCourse().getId() == cours.getId())
                    .collect(Collectors.toList());

            controller.displayQuizzes1(filteredQuizzes);

            Stage stage = (Stage) coursGrid.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // 🔥 Load and add header.fxml (Navbar)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(1500, 100); // Adjust size as needed
            mainContent.getChildren().add(headerFxmlContent);

            // Load Header Image
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
            mainContent.getChildren().add(headerImageView);

            // Load Content
            root.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(root);

            // Load Footer Image
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
            mainContent.getChildren().add(footerImageView);

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            Scene scene = new Scene(scrollPane, 1500, 700);

            // Load CSS files
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

            stage.setScene(scene);
            stage.setTitle("Quizzes du Cours: " + cours.getTitle());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement : " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

}
