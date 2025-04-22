package Controller.Oumaima;

import entite.Oumaima.Quiz;
import entite.Oumaima.Cours;
import service.Oumaima.QuizService;
import service.Oumaima.CoursService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;




import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

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
    private Button returnButton; // Added for Retour button

    private final QuizService quizService = new QuizService();
    private final CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        // Initialisation de la date
        LocalDate today = LocalDate.now();
        dateCreationField.setText(today.toString());
        dateCreationField.setEditable(false);

        // Chargement des cours dans le ComboBox
        List<Cours> coursList = coursService.readAll();
        coursComboBox.setItems(FXCollections.observableArrayList(coursList));

        // Affichage du titre des cours
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
    }

    @FXML
    private void handleCreateQuiz() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        int duration;

        // Vérification des champs
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Titre manquant", "Veuillez entrer un titre pour le quiz.");
            return;
        }

        try {
            duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0) {
                showAlert(Alert.AlertType.ERROR, "Durée invalide", "La durée doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Durée invalide", "Veuillez entrer un nombre entier pour la durée.");
            return;
        }

        Cours selectedCours = coursComboBox.getSelectionModel().getSelectedItem();
        if (selectedCours == null) {
            showAlert(Alert.AlertType.ERROR, "Cours manquant", "Veuillez sélectionner un cours.");
            return;
        }

        // Création du quiz
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setDuration(duration);
        quiz.setCreatedAt(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        quiz.setCourse(selectedCours);
        quiz.setNote(0); // Note initiale, si nécessaire

        try {
            // Sauvegarde du quiz
            quizService.create(quiz);

            // Envoi de l'email
            sendEmail(quiz);

            // Redirection vers l'ajout de questions
            openAddQuestionInterface(quiz);
            closeWindow(); // Ferme la fenêtre actuelle
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création du quiz ou de l'envoi de l'email : " + e.getMessage());
        }
    }

    private void sendEmail(Quiz quiz) {
        final String fromEmail = "oumaima.boulila@esprit.tn"; // Remplacez par @gmail.com si nécessaire
        final String password = "jmis uuzk emig hccz";
        final String toEmail = "oumaima.boulila@esprit.tn";

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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Nouveau Quiz Créé : " + quiz.getTitle());
            message.setContent("<h2>Nouveau Quiz Créé</h2>" +
                            "<p>Un nouveau quiz a été créé avec les détails suivants :</p>" +
                            "<ul>" +
                            "<li><b>Titre :</b> " + quiz.getTitle() + "</li>" +
                            "<li><b>Description :</b> " + quiz.getDescription() + "</li>" +
                            "<li><b>Durée :</b> " + quiz.getDuration() + " minutes</li>" +
                            "<li><b>Cours :</b> " + quiz.getCourse().getTitle() + "</li>" +
                            "<li><b>Date de création :</b> " + quiz.getCreatedAt() + "</li>" +
                            "</ul>" +
                            "<p>Cordialement,<br>L'équipe de gestion des quiz</p>",
                    "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
    private void openAddQuestionInterface(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OumaimaFXML/addQuestion.fxml"));
            Parent root = loader.load();

            // Utiliser le vrai nom du contrôleur
            addQuestioncontroller controller = loader.getController();
            controller.initData(quiz.getId()); // Passer l’ID du quiz

            Stage stage = new Stage();
            stage.setTitle("Ajouter des Questions");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface d'ajout de questions : " + e.getMessage());
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
            // Get the current stage
            Stage stage = (Stage) returnButton.getScene().getWindow();
            VBox mainContent = new VBox();

            // Load header.fxml
            FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
            VBox headerFxmlContent = headerFxmlLoader.load();
            headerFxmlContent.setPrefSize(1000, 100);
            mainContent.getChildren().add(headerFxmlContent);

            // Load header.html
            WebView headerWebView = new WebView();
            URL headerUrl = getClass().getResource("/header.html");
            if (headerUrl != null) {
                headerWebView.getEngine().load(headerUrl.toExternalForm());
            } else {
                headerWebView.getEngine().loadContent("<html><body><h1>Header Not Found</h1></body></html>");
            }
            headerWebView.setPrefSize(1000, 490);
            mainContent.getChildren().add(headerWebView);

            // Load body (affichageQuiz.fxml)
            FXMLLoader bodyLoader = new FXMLLoader(getClass().getResource("/OumaimaFXML/affichageQuiz.fxml"));
            Parent bodyContent = bodyLoader.load();
            bodyContent.setStyle("-fx-pref-width: 600; -fx-pref-height: 600; -fx-max-height: 600;");
            mainContent.getChildren().add(bodyContent);

            // Load footer.html
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