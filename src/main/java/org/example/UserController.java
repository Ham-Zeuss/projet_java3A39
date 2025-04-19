package org.example;

import Entity.User;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.UserService;

import java.util.ArrayList;
import java.util.List;

public class UserController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ListView<String> rolesList;
    @FXML
    private CheckBox activeCheckBox;
    @FXML
    private TableView<User> userTable;
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
    private Label rolesError;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;

    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nomColumn;
    @FXML
    private TableColumn<User, String> prenomColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> rolesColumn;
    @FXML
    private TableColumn<User, Boolean> activeColumn;

    private final UserService userService = new UserService();
    private User currentUser; // Assume this is set to the logged-in user

    @FXML
    public void initialize() {
        rolesList.setItems(FXCollections.observableArrayList(
                "ROLE_MEDECIN",
                "ROLE_ENSEIGNANT",
                "ROLE_PARENT"
        ));
        rolesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        rolesColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(String.join(", ", user.getRoles()));
        });
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        loadUsers();
        setupValidation();
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateFormWithUser(newSelection);
                    }
                });

        currentUser = new User();
        currentUser.setRoles(new ArrayList<>(List.of("ROLE_ADMIN")));
    }

    private void setupValidation() {
        nomField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
                nomError.setText("Nom invalide (lettres uniquement)");
                nomField.setStyle("-fx-border-color: red;");
            } else {
                nomError.setText("");
                nomField.setStyle("");
            }
            updateButtonStates();
        });
        prenomField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^[a-zA-ZÀ-ÿ\\-]+$")) {
                prenomError.setText("Prénom invalide (lettres uniquement)");
                prenomField.setStyle("-fx-border-color: red;");
            } else {
                prenomError.setText("");
                prenomField.setStyle("");
            }
            updateButtonStates();
        });
        emailField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                emailError.setText("Email invalide");
                emailField.setStyle("-fx-border-color: red;");
            } else {
                emailError.setText("");
                emailField.setStyle("");
            }
            updateButtonStates();
        });
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (passwordField.isEditable() && (newValue == null || !newValue.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"))) {
                passwordError.setText("8+ caractères, inclut lettre, chiffre, symbole");
                passwordField.setStyle("-fx-border-color: red;");
            } else {
                passwordError.setText("");
                passwordField.setStyle("");
            }
            validateConfirmPassword();
            updateButtonStates();
        });
        confirmPasswordField.textProperty().addListener((obs, oldValue, newValue) -> {
            validateConfirmPassword();
            updateButtonStates();
        });
        rolesList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (rolesList.getSelectionModel().getSelectedItems().isEmpty()) {
                rolesError.setText("Sélectionnez au moins un rôle");
            } else {
                rolesError.setText("");
            }
            updateButtonStates();
        });
    }

    private void validateConfirmPassword() {
        if (!passwordField.isEditable()) {
            confirmPasswordError.setText("");
            confirmPasswordField.setStyle("");
            return;
        }
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (!userService.passwordsMatch(password, confirmPassword)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            confirmPasswordField.setStyle("-fx-border-color: red;");
        } else {
            confirmPasswordError.setText("");
            confirmPasswordField.setStyle("");
        }
    }

    private void updateButtonStates() {
        boolean isValid = nomError.getText().isEmpty() &&
                prenomError.getText().isEmpty() &&
                emailError.getText().isEmpty() &&
                passwordError.getText().isEmpty() &&
                confirmPasswordError.getText().isEmpty() &&
                rolesError.getText().isEmpty() &&
                !nomField.getText().isEmpty() &&
                !prenomField.getText().isEmpty() &&
                !emailField.getText().isEmpty() &&
                !rolesList.getSelectionModel().getSelectedItems().isEmpty();

        if (passwordField.isEditable()) {
            isValid = isValid && !passwordField.getText().isEmpty() && !confirmPasswordField.getText().isEmpty();
        }

        addButton.setDisable(!isValid);
        updateButton.setDisable(!isValid || userTable.getSelectionModel().getSelectedItem() == null);
    }

    private void loadUsers() {
        userTable.getItems().setAll(userService.getAllUsers());
    }

    private void populateFormWithUser(User user) {
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        passwordField.setText("");
        confirmPasswordField.setText("");
        activeCheckBox.setSelected(user.isActive());

        // Disable password fields unless current user is admin
        boolean isAdmin = userService.isAdmin(currentUser);
        passwordField.setEditable(isAdmin);
        confirmPasswordField.setEditable(isAdmin);
        passwordField.setDisable(!isAdmin);
        confirmPasswordField.setDisable(!isAdmin);

        rolesList.getSelectionModel().clearSelection();
        user.getRoles().forEach(role -> {
            int index = rolesList.getItems().indexOf(role);
            if (index >= 0) {
                rolesList.getSelectionModel().select(index);
            }
        });

        updateButtonStates();
    }

    @FXML
    private void handleAddUser() {
        User user = new User();
        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setEmail(emailField.getText());
        user.setPassword(passwordField.getText());
        user.setRoles(new ArrayList<>(rolesList.getSelectionModel().getSelectedItems()));
        user.setActive(activeCheckBox.isSelected());

        if (userService.validateUser(user, false) && userService.passwordsMatch(passwordField.getText(), confirmPasswordField.getText())) {
            userService.addUser(user);
            loadUsers();
            clearForm();
            showSuccess("Utilisateur ajouté avec succès!");
        } else {
            showAlert("Erreur de validation", "Veuillez vérifier les informations saisies.");
        }
    }

    @FXML
    private void handleUpdateUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            selectedUser.setNom(nomField.getText());
            selectedUser.setPrenom(prenomField.getText());
            selectedUser.setEmail(emailField.getText());
            selectedUser.setRoles(new ArrayList<>(rolesList.getSelectionModel().getSelectedItems()));
            selectedUser.setActive(activeCheckBox.isSelected());

            boolean isAdmin = userService.isAdmin(currentUser);
            String newPassword = isAdmin ? passwordField.getText() : null;

            if (userService.validateUser(selectedUser, true) && (!isAdmin || userService.passwordsMatch(passwordField.getText(), confirmPasswordField.getText()))) {
                userService.updateUser(selectedUser, isAdmin, newPassword);
                loadUsers();
                clearForm();
                showSuccess("Utilisateur mis à jour avec succès!");
            } else {
                showAlert("Erreur de validation", "Veuillez vérifier les informations saisies.");
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un utilisateur à modifier.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            userService.deleteUser(selectedUser.getId());
            loadUsers();
            clearForm();
            showSuccess("Utilisateur supprimé avec succès!");
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un utilisateur à supprimer.");
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        userTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        rolesList.getSelectionModel().clearSelection();
        activeCheckBox.setSelected(true);
        nomError.setText("");
        prenomError.setText("");
        emailError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        rolesError.setText("");
        nomField.setStyle("");
        prenomField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");
        passwordField.setEditable(true);
        confirmPasswordField.setEditable(true);
        passwordField.setDisable(false);
        confirmPasswordField.setDisable(false);
        updateButtonStates();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}