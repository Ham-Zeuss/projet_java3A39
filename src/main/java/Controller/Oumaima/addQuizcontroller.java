package Controller.Oumaima;

import entite.Oumaima.Quiz;
import entite.Oumaima.Cours;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import service.Oumaima.QuizService;
import service.Oumaima.CoursService;
import entite.User; // Ajout pour utiliser la classe User
import service.UserService; // Ajout pour utiliser UserService
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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

    private final QuizService quizService = new QuizService();
    private final CoursService coursService = new CoursService();
    private final UserService userService = new UserService(); // Ajout de UserService

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
        final String password = "jqne jfjy juag cxcu";

        // Retrieve all users using the new method and filter those with the ROLE_PARENT role
        List<User> allUsers = userService.getAllUsersWithStringStatus(); // Updated method
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuestion.fxml"));
            Parent root = loader.load();

            addQuestioncontroller controller = loader.getController();
            controller.initData(quiz.getId());

            Stage stage = new Stage();
            stage.setTitle("Ajouter des Questions");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
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

            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load an image instead of header.html
            ImageView headerImageView = new ImageView();
            URL imageUrl = getClass().getResource("/images/header.png"); // Adjusted path
            if (imageUrl != null) {
                Image headerImage = new Image(imageUrl.toExternalForm());
                headerImageView.setImage(headerImage);
            } else {
                headerImageView.setImage(new Image("https://thumbs.dreamstime.com/b/stunning-hd-pic-caribbean-beach-cocktail-featuring-coconut-pineapple-set-against-palm-trees-sand-blue-sea-359953956.jpg"));
            }
            headerImageView.setFitWidth(2000);
            headerImageView.setFitHeight(300);
            headerImageView.setPreserveRatio(false);
            mainContent.getChildren().add(headerImageView);

            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            WebView footerWebView = new WebView();
            URL footerUrl = getClass().getResource("/footer.html");
            if (footerUrl != null) {
                footerWebView.getEngine().load(footerUrl.toExternalForm());
            } else {
                footerWebView.getEngine().loadContent("<html><body><h1>Footer Not Found</h1></body></html>");
            }
            footerWebView.setPrefSize(1000, 830);
            mainContent.getChildren().add(footerWebView);

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            Scene scene = new Scene(scrollPane, 600, 400);
            URL cssUrl = getClass().getResource("/OumaimaFXML/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            URL userTitlesCssUrl = getClass().getResource("/css/UserTitlesStyle.css");
            if (userTitlesCssUrl != null) {
                scene.getStylesheets().add(userTitlesCssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Liste des Quiz");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la liste des quiz : " + e.getMessage());
        }
    }
}