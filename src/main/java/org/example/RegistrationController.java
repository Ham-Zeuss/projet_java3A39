package org.example;

import javafx.scene.control.RadioButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

import javafx.stage.FileChooser;
import Form.RegistrationFormType;

public class RegistrationController implements Initializable {

    @FXML
    private ImageView childlogoView;
    @FXML
    private Button close;
    @FXML
    private ComboBox<String> gouvernorat;
    @FXML
    private TextField age;
    @FXML
    private TextField numero;
    @FXML
    private RadioButton parent, enseignant, medecin;
    @FXML
    private Label registrationMessagelabel;
    @FXML
    private Label SaisieMessagelabel;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirm_password;
    @FXML
    private Label confirmpasswordLabel;
    @FXML
    private TextField nom;
    @FXML
    private TextField prenom;
    @FXML
    private TextField email;
    @FXML
    private ToggleGroup tgSelect;
    @FXML
    private ImageView photoPreview;

    private String selectedPhotoPath;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*File childFile = new File("Images/kidslogo.png");
        Image childImage = new Image(childFile.toURI().toString());
        childlogoView.setImage(childImage);*/
    }


    @FXML
    private void handleRegistration() {
        RadioButton selectedRole = (RadioButton) tgSelect.getSelectedToggle();
        if (selectedRole != null) {
            String role = selectedRole.getText(); // "Parent", "Enseignant" ou "Médecin"
            System.out.println("Rôle sélectionné : " + role);
        } else {
            // Alerte si aucun rôle sélectionné
            System.out.println("Veuillez sélectionner un rôle.");
        }
    }

    @FXML
    public void initialize_gouvernorat() {
        gouvernorat.getItems().addAll(
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès",
                "Gafsa", "Jendouba", "Kairouan", "Kasserine", "Kébili",
                "Le Kef", "Mahdia", "Manouba", "Médenine", "Monastir",
                "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );
    }

    @FXML
    private void handleUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            selectedPhotoPath = selectedFile.getAbsolutePath();
            Image image = new Image(selectedFile.toURI().toString());
            photoPreview.setImage(image);
        }
    }

    public void registerButtonAction(ActionEvent actionEvent) {
        // Validation des champs avec RegistrationFormType

        if (!RegistrationFormType.isValidName(prenom.getText())) {
            SaisieMessagelabel.setText("Le nom est invalide.");
            return;
        }
        if (!RegistrationFormType.isValidName(nom.getText())) {
            SaisieMessagelabel.setText("Le prénom est invalide.");
            return;
        }


        if (!RegistrationFormType.isValidEmail(email.getText())) {
            SaisieMessagelabel.setText("L'adresse email est invalide.");
            return;
        }

        if (!RegistrationFormType.isValidPassword(password.getText())) {
            SaisieMessagelabel.setText("Le mot de passe est trop court.");
            return;
        }

        if (!RegistrationFormType.passwordsMatch(password.getText(), confirm_password.getText())) {
            SaisieMessagelabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        if (!RegistrationFormType.isValidAge(age.getText())) {
            SaisieMessagelabel.setText("L'âge doit être compris entre 6 et 12.");
            return;
        }
        if (!RegistrationFormType.isValidPhoneNumber(numero.getText())) {
            SaisieMessagelabel.setText("Le numéro de téléphone est invalide.");
            return;
        }

        if (!RegistrationFormType.isRoleSelected(tgSelect)) {
            SaisieMessagelabel.setText("Veuillez sélectionner un rôle.");
            return;
        }


        if (!password.getText().equals(confirm_password.getText())) {
            confirmpasswordLabel.setText("Mot de passe incorrect");
            return;
        }

        registerUser();
    }

    public void closeButtonAction(ActionEvent event) {
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    public void registerUser() {
        Connection connectDB = DataSource.getInstance().getConnection();

        String firstname = nom.getText();
        String lastname = prenom.getText();
        String username = email.getText();
        String motdepasse = password.getText();
        String confirmPassword = confirm_password.getText();
        String ageValue = age.getText();
        String gouvernoratValue = (String) gouvernorat.getValue(); // ComboBox<String>
        String numeroTel = numero.getText();
        String photoPath = selectedPhotoPath;
        RadioButton selectedRoleButton = (RadioButton) tgSelect.getSelectedToggle();

        if (firstname.isEmpty() || lastname.isEmpty() || username.isEmpty() || motdepasse.isEmpty()
                || confirmPassword.isEmpty() || ageValue.isEmpty() || gouvernoratValue == null
                || numeroTel.isEmpty() || photoPath.isEmpty() || selectedRoleButton == null) {
            registrationMessagelabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!motdepasse.equals(confirmPassword)) {
            registrationMessagelabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        int ageInt;
        try {
            ageInt = Integer.parseInt(ageValue);
        } catch (NumberFormatException e) {
            registrationMessagelabel.setText("L'âge doit être un nombre valide.");
            return;
        }


        // Définir le rôle sélectionné (Parent, Enseignant, Médecin)
        String role = switch (selectedRoleButton.getText()) {
            case "Parent" -> "[\"ROLE_PARENT\"]";
            case "Enseignant" -> "[\"ROLE_ENSEIGNANT\"]";
            case "Médecin" -> "[\"ROLE_MEDECIN\"]";
            default -> "[\"ROLE_USER\"]";
        };

        // Requête SQL
        String insertQuery = "INSERT INTO user (nom, prenom, email, roles, password, is_verified, age, gouvernorat, numero, photo, is_active) " +
                "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, 1)";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(insertQuery);
            preparedStatement.setString(1, lastname);
            preparedStatement.setString(2, firstname);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, role);
            preparedStatement.setString(5, motdepasse); // Mot de passe en clair (pas crypté)
            preparedStatement.setInt(6, ageInt);
            preparedStatement.setString(7, gouvernoratValue);
            preparedStatement.setString(8, numeroTel);
            preparedStatement.setString(9, photoPath);

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                registrationMessagelabel.setText("Utilisateur inscrit avec succès !");
            } else {
                registrationMessagelabel.setText("Échec de l'inscription.");
            }

            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            registrationMessagelabel.setText("Erreur lors de l'inscription.");
        }
    }

}
