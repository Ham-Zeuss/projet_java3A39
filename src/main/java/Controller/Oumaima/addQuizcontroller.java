package Controller.Oumaima;

import entite.Oumaima.Quiz;
import entite.Oumaima.Cours;
import service.Oumaima.QuizService;
import service.Oumaima.CoursService;
import entite.User;
import service.UserService;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.collections.FXCollections;

public class addQuizcontroller {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField durationField;

    @FXML
    private TextField dateCreationField;

    @FXML
    private ComboBox<Cours> coursComboBox;

    @FXML
    private Button returnButton;

    @FXML
    private Button addQuizButton; // Assumed to be in FXML for "Enregistrer"

    private final QuizService quizService = new QuizService();
    private final CoursService coursService = new CoursService();
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        dateCreationField.setText(today.toString());
        dateCreationField.setEditable(false);

        List<Cours> coursList = coursService.readAll();
        coursComboBox.setItems(FXCollections.observableArrayList(coursList));

        coursComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Cours cours, boolean empty) {
                super.updateItem(cours, empty);
                setText(empty || cours == null ? null : cours.getTitle());
            }
        });
        coursComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cours cours, boolean empty) {
                super.updateItem(cours, empty);
                setText(empty || cours == null ? null : cours.getTitle());
            }
        });

        // Restreindre durationField aux chiffres uniquement
        durationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                durationField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Set "Enregistrer" text and action for addQuizButton
        if (addQuizButton != null) {
            addQuizButton.setText("Enregistrer");
            addQuizButton.setOnAction(e -> handleCreateQuiz());
        }

        // Ensure returnButton triggers goBackToQuizList
        if (returnButton != null) {
            returnButton.setText("Retour");
            returnButton.setOnAction(e -> goBackToQuizList());
        }
    }

    @FXML
    private void handleCreateQuiz() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String durationText = durationField.getText().trim();
        int duration;

        // Validation du titre
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Titre manquant", "Veuillez entrer un titre pour le quiz.");
            return;
        }

        // Validation de la durée
        if (durationText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Durée manquante", "Veuillez entrer une durée pour le quiz.");
            return;
        }

        try {
            duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                showAlert(Alert.AlertType.ERROR, "Durée invalide", "La durée doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Durée invalide", "La durée doit être un nombre entier (exemple : 30).");
            return;
        }

        Cours selectedCours = coursComboBox.getSelectionModel().getSelectedItem();
        if (selectedCours == null) {
            showAlert(Alert.AlertType.ERROR, "Cours manquant", "Veuillez sélectionner un cours.");
            return;
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setDuration(duration);
        quiz.setCreatedAt(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        quiz.setCourse(selectedCours);
        quiz.setNote(0);

        try {
            quizService.create(quiz);
            System.out.println("Quiz created successfully: " + quiz.getTitle());

            sendEmail(quiz); // Envoi de l’email à tous les ROLE_PARENT

            openAddQuestionInterface(quiz);
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création du quiz ou de l’envoi de l’email : " + e.getMessage());
        }
    }

    private void sendEmail(Quiz quiz) {
        final String fromEmail = "66damm.mmad66@gmail.com";
        final String password = "tcrx etrl ypbq omsd";

        // Retrieve all users using the new method and filter those with the ROLE_PARENT role
        List<User> allUsers = userService.getAllUsersWithStringStatus();
        List<String> parentEmails = allUsers.stream()
                .filter(user -> {
                    boolean hasRole = user.getRoles().contains("ROLE_PARENT");
                    if (!hasRole) {
                        System.out.println("User " + user.getEmail() + " does not have ROLE_PARENT. Roles: " + user.getRoles());
                    }
                    return hasRole;
                })
                .map(User::getEmail)
                .collect(Collectors.toList());

        if (parentEmails.isEmpty()) {
            System.out.println("No users with ROLE_PARENT found.");
            showAlert(Alert.AlertType.WARNING, "Warning", "No users with ROLE_PARENT found to receive the email.");
            return;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));

            // Add all parent emails as BCC recipients
            InternetAddress[] recipientAddresses = parentEmails.stream()
                    .map(email -> {
                        try {
                            return new InternetAddress(email);
                        } catch (AddressException e) {
                            System.err.println("Invalid email address: " + email);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(InternetAddress[]::new);
            message.setRecipients(Message.RecipientType.BCC, recipientAddresses);

            message.setSubject("New Quiz Created: " + quiz.getTitle());
            message.setContent("<h2>New Quiz Created</h2>" +
                            "<p>A new quiz has been created with the following details:</p>" +
                            "<ul>" +
                            "<li><b>Title:</b> " + quiz.getTitle() + "</li>" +
                            "<li><b>Description:</b> " + quiz.getDescription() + "</li>" +
                            "<li><b>Duration:</b> " + quiz.getDuration() + " minutes</li>" +
                            "<li><b>Course:</b> " + quiz.getCourse().getTitle() + "</li>" +
                            "<li><b>Created At:</b> " + quiz.getCreatedAt() + "</li>" +
                            "</ul>" +
                            "<p>Best regards,<br>The Quiz Management Team</p>",
                    "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("Email sent successfully to: " + String.join(", ", parentEmails));
        } catch (MessagingException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.WARNING, "Warning", "Error sending email: " + e.getMessage());
        }
    }

    private void openAddQuestionInterface(Quiz quiz) {
        try {
            Stage stage = new Stage();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Add header image as the first element
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1500);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
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

            // 2. Load body (addQuestion.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuestion.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/addQuestion.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // Set controller data
            addQuestioncontroller controller = bodyLoader.getController();
            controller.initData(quiz.getId());

            // 3. Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1500);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
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
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Calculate required height
            double totalHeight = headerImageView.getFitHeight() +
                    bodyContent.prefHeight(-1) +
                    footerImageView.getFitHeight();

            // Set scene to specified size
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }

            URL NavBar = getClass().getResource("/navbar.css");
            if (NavBar != null) {
                scene.getStylesheets().add(NavBar.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Ajouter des Questions");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d’ouvrir l’interface d’ajout de questions : " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBackToQuizList() {
        try {
            Stage stage = (Stage) returnButton.getScene().getWindow();
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Add header image as the first element
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1500);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
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

            // 2. Load body (affichageQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/affichageQuiz.fxml introuvable");
            }
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // 3. Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1500);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
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
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Calculate required height
            double totalHeight = headerImageView.getFitHeight() +
                    bodyContent.prefHeight(-1) +
                    footerImageView.getFitHeight();

            // Set scene to specified size
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }

            URL NavBar = getClass().getResource("/navbar.css");
            if (NavBar != null) {
                scene.getStylesheets().add(NavBar.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Liste des Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la liste des quiz : " + e.getMessage());
        }
    }

    public void displayAddQuizPage(Stage stage) {
        try {
            VBox mainContent = new VBox();
            mainContent.setAlignment(Pos.TOP_CENTER); // Align all content to top center

            // 1. Add header image as the first element
            ImageView headerImageView = new ImageView();
            try {
                Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                headerImageView.setImage(headerImage);
                headerImageView.setPreserveRatio(true);
                headerImageView.setFitWidth(1500);
                headerImageView.setSmooth(true);
                headerImageView.setCache(true);
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

            // 2. Load body (addQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuiz.fxml"));
            if (bodyLoader.getLocation() == null) {
                throw new IllegalStateException("Fichier /OumaimaFXML/addQuiz.fxml introuvable");
            }
            bodyLoader.setController(this); // Set this controller to handle the FXML
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 1500; -fx-pref-height: 1080; -fx-max-height: 2000;");
            mainContent.getChildren().add(bodyContent);

            // 3. Load footer as ImageView
            ImageView footerImageView = new ImageView();
            try {
                Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                footerImageView.setImage(footerImage);
                footerImageView.setPreserveRatio(true);
                footerImageView.setFitWidth(1500);
            } catch (Exception e) {
                System.err.println("Error loading footer image: " + e.getMessage());
                Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
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
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Calculate required height
            double totalHeight = headerImageView.getFitHeight() +
                    bodyContent.prefHeight(-1) +
                    footerImageView.getFitHeight();

            // Set scene to specified size
            Scene scene = new Scene(scrollPane, 1500, 700);

            // Add CSS files
            URL storeCards = getClass().getResource("/css/store-cards.css");
            if (storeCards != null) {
                scene.getStylesheets().add(storeCards.toExternalForm());
            }

            URL NavBar = getClass().getResource("/navbar.css");
            if (NavBar != null) {
                scene.getStylesheets().add(NavBar.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Ajouter un Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page d'ajout de quiz : " + e.getMessage());
        }
    }
}