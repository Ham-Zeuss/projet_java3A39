package org.example;

import entite.User;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.UserService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private TextField searchField;
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
    private Label totalUsersLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label roleUsersLabel;
    @FXML
    private PieChart rolesPieChart;
    @FXML
    private VBox totalUsersCard;
    @FXML
    private VBox activeInactiveCard;
    @FXML
    private VBox roleUsersCard;

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
    private User currentUser;

    @FXML
    public void initialize() {
        rolesList.setItems(FXCollections.observableArrayList(
                "ROLE_MEDECIN", "ROLE_ENSEIGNANT", "ROLE_PARENT"
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
        setupSearch();
        setupStatistics();
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateFormWithUser(newSelection);
                    }
                });

        currentUser = new User();
        currentUser.setRoles(new ArrayList<>(List.of("ROLE_ADMIN")));
        configurePieChart();
    }

    private void setupStatistics() {
        updateStatistics();
    }

    private void updateStatistics() {
        List<User> users = userService.getAllUsers();

        // Total Users
        totalUsersLabel.setText(String.valueOf(users.size()));

        // Active/Inactive Users
        long activeCount = users.stream().filter(User::isActive).count();
        long inactiveCount = users.size() - activeCount;
        activeUsersLabel.setText(activeCount + " / " + inactiveCount);

        // Users by Role (e.g., ROLE_MEDECIN)
        long roleCount = users.stream()
                .filter(user -> user.getRoles().contains("ROLE_MEDECIN"))
                .count();
        roleUsersLabel.setText(String.valueOf(roleCount));

        // Role Distribution
        Map<String, Integer> roleDistribution = new HashMap<>();
        for (User user : users) {
            for (String role : user.getRoles()) {
                roleDistribution.put(role, roleDistribution.getOrDefault(role, 0) + 1);
            }
        }
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        roleDistribution.forEach((role, count) -> {
            PieChart.Data data = new PieChart.Data(role, count);
            pieChartData.add(data);
        });
        rolesPieChart.setData(pieChartData);
    }
    private void configurePieChart() {
        // Ensure labels are visible and show role names
        rolesPieChart.setLabelsVisible(true);
        rolesPieChart.setLabelLineLength(10); // Shorten the label lines for better appearance
        rolesPieChart.setLegendVisible(true); // Show legend for clarity

        // Customize each slice to ensure the role name is displayed
        for (PieChart.Data data : rolesPieChart.getData()) {
            data.getNode().setStyle("-fx-pie-label-visible: true;");
            // Optionally, set the name again to ensure it’s displayed (redundant but ensures compatibility)
            data.nameProperty().set(data.getName());
        }
    }

    @FXML
    private void handleTotalUsersClick() {
        loadUsers();
        searchField.clear();
    }

    @FXML
    private void handleActiveInactiveClick() {
        List<User> activeUsers = userService.getAllUsers().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
        userTable.getItems().setAll(activeUsers);
        searchField.clear();
    }

    @FXML
    private void handleRoleUsersClick() {
        List<User> roleUsers = userService.getAllUsers().stream()
                .filter(user -> user.getRoles().contains("ROLE_MEDECIN"))
                .collect(Collectors.toList());
        userTable.getItems().setAll(roleUsers);
        searchField.clear();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void filterUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadUsers();
        } else {
            List<User> filteredUsers = userService.searchUsers(query.trim());
            userTable.getItems().setAll(filteredUsers);
        }
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
        updateStatistics();
    }

    private void populateFormWithUser(User user) {
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        passwordField.setText("");
        confirmPasswordField.setText("");
        activeCheckBox.setSelected(user.isActive());

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
        String email = emailField.getText();
        if (userService.emailExists(email)) {
            showAlert("Erreur", "Cet email est déjà utilisé.");
            return;
        }

        User user = new User();
        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setEmail(email);
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
            String newEmail = emailField.getText();
            if (!newEmail.equals(selectedUser.getEmail()) && userService.emailExists(newEmail)) {
                showAlert("Erreur", "Cet email est déjà utilisé.");
                return;
            }

            selectedUser.setNom(nomField.getText());
            selectedUser.setPrenom(prenomField.getText());
            selectedUser.setEmail(newEmail);
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
    private void handleShowNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/notifications.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Notifications");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la vue des notifications: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            if (userService.hasAssociatedCours(selectedUser.getId())) {
                showAlert("Suppression impossible",
                        "Cet utilisateur a des cours associés. Veuillez supprimer ou réassigner les cours avant de supprimer l'utilisateur.");
                return;
            }
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