package Controller.Oumaima;

import entite.Oumaima.Cours;
import entite.Oumaima.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import service.Oumaima.CoursService;
import service.Oumaima.QuizService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class UpdateQuizController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField durationField;

    @FXML
    private Button returnButton;

    @FXML
    private ComboBox<Cours> courseComboBox;

    @FXML
    private Button updateButton;

    private final QuizService quizService = new QuizService();
    private final CoursService courseService = new CoursService();

    private Quiz quizToUpdate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Cours> courses = courseService.readAll();
        courseComboBox.getItems().addAll(courses);

        // Personnalisation de l'affichage dans la ComboBox
        courseComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });
        courseComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });

        // Configure buttons with icons and text
        setupButton(updateButton, "https://img.icons8.com/?size=100&id=94194&format=png&color=000000", "Enregistrer", false);
        setupButton(returnButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Retour", false);

        // Set actions for buttons
        updateButton.setOnAction(event -> updateQuiz());
        returnButton.setOnAction(event -> returnToAffichageQuiz());
    }

    public void setQuizToUpdate(Quiz quiz) {
        this.quizToUpdate = quiz;

        titleField.setText(quiz.getTitle());
        descriptionArea.setText(quiz.getDescription());
        durationField.setText(String.valueOf(quiz.getDuration()));

        if (quiz.getCourse() != null) {
            courseComboBox.getSelectionModel().select(quiz.getCourse());
        }
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(70);
            icon.setFitHeight(70);
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

    private void updateQuiz() {
        if (quizToUpdate != null) {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            int duration;
            try {
                duration = Integer.parseInt(durationField.getText());
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "La durée doit être un nombre valide.");
                alert.showAndWait();
                return;
            }
            Cours selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();

            // Update the quiz object
            quizToUpdate.setTitle(title);
            quizToUpdate.setDescription(description);
            quizToUpdate.setDuration(duration);
            quizToUpdate.setCourse(selectedCourse);

            // Save the updated quiz
            quizService.update(quizToUpdate);

            try {
                // Use screen size
                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getVisualBounds();
                double width = bounds.getWidth();
                double height = bounds.getHeight();

                Stage currentStage = (Stage) updateButton.getScene().getWindow();
                VBox mainContent = new VBox();
                mainContent.setAlignment(Pos.TOP_CENTER);



                // Add header image
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(width);
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(headerImageView);

                // Load body content
                FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
                if (bodyLoader.getLocation() == null) {
                    throw new IllegalStateException("Fichier /OumaimaFXML/affichageQuiz.fxml introuvable");
                }
                Parent bodyContent = bodyLoader.load();
                bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
                bodyContent.getStyleClass().add("body-content");
                mainContent.getChildren().add(bodyContent);

                // Add footer image
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(width);
                } catch (Exception e) {
                    System.err.println("Error loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                    Label errorLabel = new Label("Footer image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(footerImageView);

                // Wrap the VBox in a ScrollPane
                ScrollPane scrollPane = new ScrollPane(mainContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                // Create scene
                Scene scene = new Scene(scrollPane, width, height);

                // Add CSS files
                URL storeCards = getClass().getResource("/css/store-cards.css");
                if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
                URL navBar = getClass().getResource("/navbar.css");
                if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
                URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
                if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());
                URL appointments = getClass().getResource("/css/appointments.css");
                if (appointments != null) scene.getStylesheets().add(appointments.toExternalForm());
                URL gooButton = getClass().getResource("/css/GooButton.css");
                if (gooButton != null) scene.getStylesheets().add(gooButton.toExternalForm());
                URL gamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
                if (gamesMenuStyling != null) scene.getStylesheets().add(gamesMenuStyling.toExternalForm());
                URL profileCard = getClass().getResource("/css/profile-card.css");
                if (profileCard != null) scene.getStylesheets().add(profileCard.toExternalForm());
                URL designFull = getClass().getResource("/DesignFull.css");
                if (designFull != null) scene.getStylesheets().add(designFull.toExternalForm());
                URL commentsStyle = getClass().getResource("/css/CommentsStyle.css");
                if (commentsStyle != null) scene.getStylesheets().add(commentsStyle.toExternalForm());
                URL oumaimaStyle = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
                if (oumaimaStyle != null) scene.getStylesheets().add(oumaimaStyle.toExternalForm());

                currentStage.setScene(scene);
                currentStage.setTitle("Liste des Quiz");
                currentStage.setResizable(true);
                currentStage.centerOnScreen();
                currentStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la liste des quiz : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void returnToAffichageQuiz() {
        try {
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            Stage currentStage = (Stage) returnButton.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);



            // Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(width);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load body content
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/affichageQuiz.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Add footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(width);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap the VBox in a ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Create scene
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
            URL navBar = getClass().getResource("/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());
            URL appointments = getClass().getResource("/css/appointments.css");
            if (appointments != null) scene.getStylesheets().add(appointments.toExternalForm());
            URL gooButton = getClass().getResource("/css/GooButton.css");
            if (gooButton != null) scene.getStylesheets().add(gooButton.toExternalForm());
            URL gamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStyling != null) scene.getStylesheets().add(gamesMenuStyling.toExternalForm());
            URL profileCard = getClass().getResource("/css/profile-card.css");
            if (profileCard != null) scene.getStylesheets().add(profileCard.toExternalForm());
            URL designFull = getClass().getResource("/DesignFull.css");
            if (designFull != null) scene.getStylesheets().add(designFull.toExternalForm());
            URL commentsStyle = getClass().getResource("/css/CommentsStyle.css");
            if (commentsStyle != null) scene.getStylesheets().add(commentsStyle.toExternalForm());
            URL oumaimaStyle = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (oumaimaStyle != null) scene.getStylesheets().add(oumaimaStyle.toExternalForm());

            currentStage.setScene(scene);
            currentStage.setTitle("Liste des Quiz");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la liste des quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void displayUpdateQuizPage(Stage stage, Quiz quiz) {
        try {
            setQuizToUpdate(quiz); // Set the quiz to update
            // Use screen size
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER);

            // Load navbar (header.fxml)
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerLoader.load();
            headerFxmlContent.setPrefSize(width * 0.6, 100);
            VBox.setMargin(headerFxmlContent, new Insets(0, 0, 10, 0));
            mainContent.getChildren().add(headerFxmlContent);

            // Add header image
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(width);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
                VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
            } catch (Exception e) {
                System.err.println("Error loading header image: " + e.getMessage());
                Rectangle fallbackHeader = new Rectangle(width * 0.6, 150, Color.LIGHTGRAY);
                Label errorLabel = new Label("Header image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(headerImageView);

            // Load body content
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/updateQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/updateQuiz.fxml introuvable");
            }
            bodyLoader.setController(this); // Set this controller to handle the FXML
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: " + width + "; -fx-pref-height: " + height + "; -fx-max-height: 2000;");
            bodyContent.getStyleClass().add("body-content");
            mainContent.getChildren().add(bodyContent);

            // Add footer image
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(width);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(width * 0.6, 100, Color.LIGHTGRAY);
                Label errorLabel = new Label("Footer image not found");
                errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                mainContent.getChildren().add(fallbackBox);
            }
            mainContent.getChildren().add(footerImageView);

            // Wrap the VBox in a ScrollPane
            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Create scene
            Scene scene = new Scene(scrollPane, width, height);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) scene.getStylesheets().add(storeCards.toExternalForm());
            URL navBar = getClass().getResource("/navbar.css");
            if (navBar != null) scene.getStylesheets().add(navBar.toExternalForm());
            URL affichageprofilefront = getClass().getResource("/css/affichageprofilefront.css");
            if (affichageprofilefront != null) scene.getStylesheets().add(affichageprofilefront.toExternalForm());
            URL appointments = getClass().getResource("/css/appointments.css");
            if (appointments != null) scene.getStylesheets().add(appointments.toExternalForm());
            URL gooButton = getClass().getResource("/css/GooButton.css");
            if (gooButton != null) scene.getStylesheets().add(gooButton.toExternalForm());
            URL gamesMenuStyling = getClass().getResource("/css/GamesMenuStyling.css");
            if (gamesMenuStyling != null) scene.getStylesheets().add(gamesMenuStyling.toExternalForm());
            URL profileCard = getClass().getResource("/css/profile-card.css");
            if (profileCard != null) scene.getStylesheets().add(profileCard.toExternalForm());
            URL designFull = getClass().getResource("/DesignFull.css");
            if (designFull != null) scene.getStylesheets().add(designFull.toExternalForm());
            URL commentsStyle = getClass().getResource("/css/CommentsStyle.css");
            if (commentsStyle != null) scene.getStylesheets().add(commentsStyle.toExternalForm());
            URL oumaimaStyle = getClass().getResource("/OumaimaFXML/oumaimastyle.css");
            if (oumaimaStyle != null) scene.getStylesheets().add(oumaimaStyle.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Modifier un Quiz");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la page de modification de quiz : " + e.getMessage());
            alert.showAndWait();
        }
    }
}