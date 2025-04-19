package org.example;
import com.gluonhq.charm.glisten.control.TextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class SecurityController implements Initializable {

    @FXML
    private Button annuler;
    @FXML
    private Label connAlerte;
    @FXML
    private ImageView imageView;
    @FXML
    private ImageView logoImageView;
    @FXML
    private PasswordField _password;
    @FXML
    private TextField _username;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Chargement des images
        File brandingFile = new File("Images/contact_1.png");
        Image brandingImage = new Image(brandingFile.toURI().toString());
        imageView.setImage(brandingImage);

        File viewFile = new File("Images/kidslightlogo.png");
        Image viewImage = new Image(viewFile.toURI().toString());
        logoImageView.setImage(viewImage);
    }

    public void connexionButtonAction(ActionEvent event) {
        if (_password.getText().trim().isEmpty() || _username.getText().trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de connexion", "Veuillez remplir tous les champs !");
        } else {
            showAlert(AlertType.INFORMATION, "Connexion", "Vous êtes sur le point de vous connecter !");
        }
    }

    public void annnulerButtonClicked(ActionEvent event) {
        Stage stage = (Stage) annuler.getScene().getWindow();
        stage.close();
    }

    public void validateloginButtonClicked(ActionEvent event) {
        // 🔹 Récupération de la connexion
        Connection connectDB = DataSource.getInstance().getConnection();
        String verifyLogin = "SELECT count(1) FROM user WHERE email = ? AND password = ?";

        try (PreparedStatement statement = connectDB.prepareStatement(verifyLogin)) {
            statement.setString(1, _username.getText().trim());
            statement.setString(2, _password.getText().trim());

            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next() && queryResult.getInt(1) == 1) {
                    //connAlerte.setText("✅ Connexion réussie !");
                    createAccountForm();
                } else {
                    connAlerte.setText("❌ Identifiants incorrects, veuillez réessayer.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            connAlerte.setText("⚠️ Erreur de connexion à la base de données.");
        }
    }

    public void createAccountForm() {
        try {
            //Navigate from login to register
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/register.fxml")));
            Stage registerstage = new Stage();
            registerstage.initStyle(StageStyle.UNDECORATED);
            registerstage.setScene(new Scene(root, 588, 722));
            registerstage.show();

        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
    }

    // Méthode pour afficher des alertes
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
