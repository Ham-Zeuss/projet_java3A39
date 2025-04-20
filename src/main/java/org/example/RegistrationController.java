package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.stage.StageStyle;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.ResourceBundle;

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
    private Label nomError;
    @FXML
    private Label prenomError;
    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmPasswordError;
    @FXML
    private Label numeroError;
    @FXML
    private Label roleError;
    @FXML
    private Label gouvernoratError;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirm_password;
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
    @FXML
    private Button registerButton;

    private String selectedPhotoPath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initialize_gouvernorat();
        setupValidation();
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

    private void setupValidation() {
        // Validation du nom
        nom.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
                nomError.setText("Nom invalide (lettres uniquement)");
                nom.setStyle("-fx-border-color: red;");
            } else {
                nomError.setText("");
                nom.setStyle("");
            }
            updateButtonState();
        });

        // Validation du prénom
        prenom.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
                prenomError.setText("Prénom invalide (lettres uniquement)");
                prenom.setStyle("-fx-border-color: red;");
            } else {
                prenomError.setText("");
                prenom.setStyle("");
            }
            updateButtonState();
        });

        // Validation de l'email
        email.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                emailError.setText("Email invalide");
                email.setStyle("-fx-border-color: red;");
            } else {
                emailError.setText("");
                email.setStyle("");
            }
            updateButtonState();
        });

        // Validation du mot de passe
        password.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
                passwordError.setText("8+ caractères, inclut lettre, chiffre, symbole");
                password.setStyle("-fx-border-color: red;");
            } else {
                passwordError.setText("");
                password.setStyle("");
            }
            validateConfirmPassword();
            updateButtonState();
        });

        // Validation de la confirmation du mot de passe
        confirm_password.textProperty().addListener((obs, oldValue, newValue) -> {
            validateConfirmPassword();
            updateButtonState();
        });

        // Validation du numéro de téléphone
        numero.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^[0-9]{8}$")) {
                numeroError.setText("Numéro invalide (8 chiffres)");
                numero.setStyle("-fx-border-color: red;");
            } else {
                numeroError.setText("");
                numero.setStyle("");
            }
            updateButtonState();
        });

        // Validation du rôle
        tgSelect.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                roleError.setText("Sélectionnez un rôle");
            } else {
                roleError.setText("");
            }
            updateButtonState();
        });

        // Validation du gouvernorat
        gouvernorat.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                gouvernoratError.setText("Sélectionnez un gouvernorat");
            } else {
                gouvernoratError.setText("");
            }
            updateButtonState();
        });

        // Validation de l'âge
        age.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                int ageValue = Integer.parseInt(newValue);
                if (ageValue < 6 || ageValue > 12) {
                    age.setStyle("-fx-border-color: red;");
                } else {
                    age.setStyle("");
                }
            } catch (NumberFormatException e) {
                age.setStyle("-fx-border-color: red;");
            }
            updateButtonState();
        });
    }

    private void validateConfirmPassword() {
        String passwordText = password.getText();
        String confirmPasswordText = confirm_password.getText();
        if (!passwordText.equals(confirmPasswordText)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            confirm_password.setStyle("-fx-border-color: red;");
        } else {
            confirmPasswordError.setText("");
            confirm_password.setStyle("");
        }
    }

    private void updateButtonState() {
        boolean isValid = nomError.getText().isEmpty() &&
                prenomError.getText().isEmpty() &&
                emailError.getText().isEmpty() &&
                passwordError.getText().isEmpty() &&
                numeroError.getText().isEmpty() &&
                roleError.getText().isEmpty() &&
                confirmPasswordError.getText().isEmpty() &&
                gouvernoratError.getText().isEmpty() &&
                !nom.getText().isEmpty() &&
                !prenom.getText().isEmpty() &&
                !email.getText().isEmpty() &&
                !password.getText().isEmpty() &&
                !numero.getText().isEmpty() &&
                tgSelect.getSelectedToggle() != null &&
                gouvernorat.getValue() != null;

        try {
            int ageValue = Integer.parseInt(age.getText());
            isValid = isValid && (ageValue >= 6 && ageValue <= 12);
        } catch (NumberFormatException e) {
            isValid = false;
        }

        // Photo est facultative, mais si elle est sélectionnée, elle doit être valide
        isValid = isValid && (selectedPhotoPath != null || true); // Rendre la photo facultative

        registerButton.setDisable(!isValid);
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
            updateButtonState();
        }
    }

    @FXML
    public void registerButtonAction(ActionEvent actionEvent) {
        registerUser();
    }

    @FXML
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
        String gouvernoratValue = gouvernorat.getValue();
        String numeroTel = numero.getText();
        String photoPath = selectedPhotoPath;
        RadioButton selectedRoleButton = (RadioButton) tgSelect.getSelectedToggle();

        if (firstname.isEmpty() || lastname.isEmpty() || username.isEmpty() || motdepasse.isEmpty()
                || confirmPassword.isEmpty() || ageValue.isEmpty() || gouvernoratValue == null
                || numeroTel.isEmpty() || selectedRoleButton == null) {
            registrationMessagelabel.setText("Veuillez remplir tous les champs obligatoires.");
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

        String hashedPassword = BCrypt.withDefaults().hashToString(12, motdepasse.toCharArray());


        String role = switch (selectedRoleButton.getText()) {
            case "Parent" -> "[\"ROLE_PARENT\"]";
            case "Enseignant" -> "[\"ROLE_ENSEIGNANT\"]";
            case "Médecin" -> "[\"ROLE_MEDECIN\"]";
            default -> "[\"ROLE_USER\"]";
        };

        String insertQuery = "INSERT INTO user (nom, prenom, email, roles, password, is_verified, age, gouvernorat, numero, photo, is_active) " +
                "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, 1)";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(insertQuery);
            preparedStatement.setString(1, lastname);
            preparedStatement.setString(2, firstname);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, role);
            preparedStatement.setString(5, hashedPassword); // TODO: Hash password
            preparedStatement.setInt(6, ageInt);
            preparedStatement.setString(7, gouvernoratValue);
            preparedStatement.setString(8, numeroTel);
            preparedStatement.setString(9, photoPath == null ? "" : photoPath);

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                registrationMessagelabel.setText("Utilisateur inscrit avec succès !");
                clearForm();
            } else {
                registrationMessagelabel.setText("Échec de l'inscription.");
            }

            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            registrationMessagelabel.setText("Erreur lors de l'inscription.");
        }
    }

    public void createAccountForm(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/login.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.sizeToScene();
            currentStage.setResizable(false);
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : " + e.getCause());
        }
    }

    private void clearForm() {
        nom.clear();
        prenom.clear();
        email.clear();
        password.clear();
        confirm_password.clear();
        age.clear();
        numero.clear();
        gouvernorat.setValue(null);
        tgSelect.selectToggle(null);
        photoPreview.setImage(null);
        selectedPhotoPath = null;
        nomError.setText("");
        prenomError.setText("");
        confirm_password.setText("");
        emailError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        numeroError.setText("");
        roleError.setText("");
        gouvernoratError.setText("");
        nom.setStyle("");
        prenom.setStyle("");
        email.setStyle("");
        password.setStyle("");
        confirm_password.setStyle("");
        age.setStyle("");
        numero.setStyle("");
        registrationMessagelabel.setText("");
        updateButtonState();
    }
}