package org.example;

import entite.Session;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.gluonhq.charm.glisten.control.TextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.control.ScrollPane;
import org.json.JSONArray;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.control.ScrollPane;
import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import test.Sidebar;
import javafx.scene.layout.BorderPane;
import java.util.function.Consumer;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;


import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class SecurityController {

    @FXML
    private Button annuler;

    @FXML
    private Label connAlerte;

    @FXML
    private PasswordField _password;

    @FXML
    private TextField _username;

    public void connexionButtonAction(ActionEvent event) {
        if (_password.getText().trim().isEmpty() || _username.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Veuillez remplir tous les champs !");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Connexion", "Vous êtes sur le point de vous connecter !");
        }
    }

    public void annnulerButtonClicked(ActionEvent event) {
        Stage stage = (Stage) annuler.getScene().getWindow();
        stage.close();
    }

    public void validateloginButtonClicked(ActionEvent event) throws SQLException {
        // Backdoor: Check if email and password are both "x"


        if (_username.getText().trim().equals("x") && _password.getText().trim().equals("x")) {
            try {
                // Set default session for backdoor (ID=14)
                Session session = Session.getInstance();
                session.setUser(48, "slim@gmail.com", "ROLE_PARENT");

                // Get screen dimensions
                Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                double screenWidth = screenBounds.getWidth();
                double screenHeight = screenBounds.getHeight();

                // Create a VBox to stack header, body, and footer
                VBox mainContent = new VBox();
                mainContent.setAlignment(Pos.TOP_CENTER);

                // 1. Load header.fxml
                FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                VBox headerFxmlContent = headerFxmlLoader.load();
                headerFxmlContent.setPrefSize(screenWidth * 0.6, 100); // Scale header width (e.g., 60% of screen width)
                mainContent.getChildren().add(headerFxmlContent);

                // 2. Load header.png
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(screenWidth); // Use screen width
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(screenWidth * 0.6, 150, Color.LIGHTGRAY); // Scale fallback
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(headerImageView);

                // 3. Load body (ListStoreItemsFront.fxml)
                Parent bodyContent;
                String fxmlPath = "HamzaFXML/ListStoreItemsFront.fxml";
                URL resourceUrl = getClass().getResource("/" + fxmlPath);
                if (resourceUrl == null) {
                    throw new Exception("Resource not found: /" + fxmlPath);
                }
                FXMLLoader bodyLoader = new FXMLLoader(resourceUrl);
                bodyContent = bodyLoader.load();
                bodyContent.setStyle("-fx-pref-width: " + screenWidth + "; -fx-pref-height: " + screenHeight + "; -fx-max-height: 2000;"); // Use screen dimensions
                mainContent.getChildren().add(bodyContent);

                // 4. Load footer.png
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(screenWidth); // Use screen width
                } catch (Exception e) {
                    System.err.println("Error loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(screenWidth * 0.6, 100, Color.LIGHTGRAY); // Scale fallback
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

                // Create scene with dynamic size
                Scene scene = new Scene(scrollPane, screenWidth, screenHeight);
                // Add CSS files
                String[] cssFiles = {
                        "/css/store-cards.css",
                        "/navbar.css",
                        "/css/styles.css",
                        "/css/UserTitlesStyle.css",
                        "/css/leaderboard.css"
                };
                for (String cssPath : cssFiles) {
                    URL cssUrl = getClass().getResource(cssPath);
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }
                }

                // Set stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Store");
                currentStage.setResizable(true);
                currentStage.centerOnScreen();
                currentStage.show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page du magasin : " + e.getMessage());
                return;
            }
        }



        if (_username.getText().trim().equals("d") && _password.getText().trim().equals("d")) {
            try {
                // Set default session for backdoor (ID=14)
                Session session = Session.getInstance();
                session.setUser(40, "johnwick@gmail.com", "ROLE_MEDECIN");

                // Get screen dimensions
                Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                double screenWidth = screenBounds.getWidth();
                double screenHeight = screenBounds.getHeight();

                // Create a VBox to stack header, body, and footer
                VBox mainContent = new VBox();
                mainContent.setAlignment(Pos.TOP_CENTER);



                // 2. Load header.png
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(screenWidth); // Use screen width
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(screenWidth * 0.6, 150, Color.LIGHTGRAY); // Scale fallback
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(headerImageView);

                // 3. Load body (ListStoreItemsFront.fxml)
                Parent bodyContent;
                String fxmlPath = "MaryemFXML/FrontDoctorsDisplayProfiles.fxml";
                URL resourceUrl = getClass().getResource("/" + fxmlPath);
                if (resourceUrl == null) {
                    throw new Exception("Resource not found: /" + fxmlPath);
                }
                FXMLLoader bodyLoader = new FXMLLoader(resourceUrl);
                bodyContent = bodyLoader.load();
                bodyContent.setStyle("-fx-pref-width: " + screenWidth + "; -fx-pref-height: " + screenHeight + "; -fx-max-height: 2000;"); // Use screen dimensions
                mainContent.getChildren().add(bodyContent);

                // 4. Load footer.png
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(screenWidth); // Use screen width
                } catch (Exception e) {
                    System.err.println("Error loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(screenWidth * 0.6, 100, Color.LIGHTGRAY); // Scale fallback
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

                // Create scene with dynamic size
                Scene scene = new Scene(scrollPane, screenWidth, screenHeight);
                // Add CSS files
                String[] cssFiles = {
                        "/css/store-cards.css",
                        "/navbar.css",
                        "/css/styles.css",
                        "/css/UserTitlesStyle.css",
                        "/css/leaderboard.css"
                };
                for (String cssPath : cssFiles) {
                    URL cssUrl = getClass().getResource(cssPath);
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }
                }

                // Set stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Store");
                currentStage.setResizable(true);
                currentStage.centerOnScreen();
                currentStage.show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page du magasin : " + e.getMessage());
                return;
            }
        }



        if (_username.getText().trim().equals("h") && _password.getText().trim().equals("h")) {
            try {
                // Set default session for backdoor (ID=14)
                Session session = Session.getInstance();
                session.setUser(45, "hedyene@esprit.tn", "ROLE_ENSEIGNANT");

                // Get screen dimensions
                Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                double screenWidth = screenBounds.getWidth();
                double screenHeight = screenBounds.getHeight();

                // Create a VBox to stack header, body, and footer
                VBox mainContent = new VBox();
                mainContent.setAlignment(Pos.TOP_CENTER);

                // 2. Load header.png
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(screenWidth);
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(screenWidth * 0.6, 150, Color.LIGHTGRAY);
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(headerImageView);

                // 3. Load body (ListStoreItemsFront.fxml)
                Parent bodyContent;
                String fxmlPath = "HedyFXML/AffichageModule.fxml";
                URL resourceUrl = getClass().getResource("/" + fxmlPath);
                if (resourceUrl == null) {
                    throw new Exception("Resource not found: /" + fxmlPath);
                }
                FXMLLoader bodyLoader = new FXMLLoader(resourceUrl);
                bodyContent = bodyLoader.load();
                bodyContent.setStyle("-fx-pref-width: " + screenWidth + "; -fx-pref-height: " + screenHeight + "; -fx-max-height: 2000;");
                mainContent.getChildren().add(bodyContent);

                // 4. Load footer.png
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(screenWidth);
                } catch (Exception e) {
                    System.err.println("Error loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(screenWidth * 0.6, 100, Color.LIGHTGRAY);
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
                Scene scene = new Scene(scrollPane, screenWidth, screenHeight);
                // Add CSS files
                String[] cssFiles = {
                        "/css/store-cards.css",
                        "/navbar.css",
                        "/css/styles.css",
                        "/css/UserTitlesStyle.css",
                        "/css/leaderboard.css"
                };
                for (String cssPath : cssFiles) {
                    URL cssUrl = getClass().getResource(cssPath);
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }
                }

                // Set stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Store");
                currentStage.setResizable(true);

                currentStage.centerOnScreen();
                currentStage.show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page du magasin : " + e.getMessage());
                return;
            }
        }


        // Backdoor: Check if email and password are both "y"
        if (_username.getText().trim().equals("y") && _password.getText().trim().equals("y")) {
            try {
                // Set admin session for backdoor
                Session session = Session.getInstance();
                session.setUser(42, "admin@gmail.com", "ROLE_ADMIN");

                // Load admin dashboard
                loadAdminDashboard(event);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le tableau de bord admin : " + e.getMessage());
                return;
            }
        }

        // Normal login logic
        Connection connectDB = DataSource.getInstance().getConnection();
        String verifyLogin = "SELECT id, email, password, roles FROM user WHERE email = ?";
        try (PreparedStatement statement = connectDB.prepareStatement(verifyLogin)) {
            statement.setString(1, _username.getText().trim());

            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    String storedHashedPassword = queryResult.getString("password");
                    int userId = queryResult.getInt("id");
                    String email = queryResult.getString("email");
                    String rolesJson = queryResult.getString("roles");

                    // Verify password
                    if (BCrypt.verifyer().verify(_password.getText().trim().toCharArray(), storedHashedPassword).verified) {
                        // Parse role from JSON
                        String role = parseRoleFromJson(rolesJson);

                        // Set session
                        Session session = Session.getInstance();
                        session.setUser(userId, email, role);

                        // Redirect to 2FA
                        DoubleAuthentication(event);
                        return;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Connexion", "Échec de connexion : Mot de passe incorrect");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Connexion", "Échec de connexion : Email non trouvé");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    private void loadAdminDashboard(ActionEvent event) throws Exception {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Create sidebar
        Consumer<String> loadFXMLConsumer = fxmlPath -> {
            try {
                loadFXML(currentStage, fxmlPath);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + e.getMessage());
            }
        };

        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                currentStage,
                () -> {
                    try {
                        loadAdminDashboard(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                () -> loadFXMLConsumer.accept("/User/index_user.fxml"),
                () -> loadFXMLConsumer.accept("/HamzaFXML/ListPexelWords.fxml"),
                () -> {
                    loadFXMLConsumer.accept("/User/login.fxml");
                },
                loadFXMLConsumer
        );

        // Create main content
        VBox mainContent = createMainContent();

        // Set up layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");
        root.setLeft(sidebar);
        root.setCenter(mainContent);

        // Create scene with dynamic size
        Scene scene = new Scene(root, screenWidth, screenHeight);
        URL cssUrl = getClass().getResource("/css/dashboard-sidebar.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Configure stage
        currentStage.setScene(scene);
        currentStage.setTitle("Admin Dashboard");
        currentStage.setResizable(true);

        currentStage.centerOnScreen();
        currentStage.show();
    }

    private void loadFXML(Stage stage, String fxmlPath) throws Exception {
        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent fxmlContent = loader.load();

        Consumer<String> loadFXMLConsumer = path -> {
            try {
                loadFXML(stage, path);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + e.getMessage());
            }
        };

        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                stage,
                () -> {
                    try {
                        loadAdminDashboard(new ActionEvent(stage, null));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                () -> loadFXMLConsumer.accept("/User/index_user.fxml"),
                () -> loadFXMLConsumer.accept("/HamzaFXML/ListPexelWords.fxml"),
                () -> {
                    loadFXMLConsumer.accept("/User/login.fxml");
                },
                loadFXMLConsumer
        );

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");
        root.setLeft(sidebar);
        root.setCenter(fxmlContent);

        Scene scene = new Scene(root, screenWidth, screenHeight);
        URL cssUrl = getClass().getResource("/css/dashboard-sidebar.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.setResizable(true);

        stage.centerOnScreen();
        stage.show();
    }
    private VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #F7F7F7;");

        Label headerLabel = new Label("Analytics dashboard");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.BLACK);

        Label subHeaderLabel = new Label("Demographic properties of your customer");
        subHeaderLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subHeaderLabel.setTextFill(Color.web("#666666"));

        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER_LEFT);

        VBox card1 = createCard("All User", "10,234", Color.web("#C20114"));
        VBox card2 = createCard("Event Count", "536", Color.web("#FF8C00"));
        VBox card3 = createCard("Conversations", "21", Color.web("#00FF00"));
        VBox card4 = createCard("New User", "3321", Color.web("#00BFFF"));

        cards.getChildren().addAll(card1, card2, card3, card4);

        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER_LEFT);

        VBox stat1 = createStatBox("Sessions", "6,132", "vs Previous 30 Days");
        VBox stat2 = createStatBox("Page Views", "11,236", "vs Previous 30 Days");
        stats.getChildren().addAll(stat1, stat2);

        content.getChildren().addAll(headerLabel, subHeaderLabel, cards, stats);
        return content;
    }

    private VBox createCard(String title, String value, Color accentColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(150);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#666666"));
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.BLACK);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Circle dot = new Circle(5, accentColor);
        card.getChildren().addAll(titleLabel, valueLabel, dot);
        return card;
    }

    private VBox createStatBox(String title, String value, String subtitle) {
        VBox statBox = new VBox(10);
        statBox.setPadding(new Insets(15));
        statBox.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.setPrefWidth(150);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#666666"));
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.BLACK);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setTextFill(Color.web("#666666"));
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

        statBox.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return statBox;
    }

    public void DoubleAuthentication(ActionEvent event) throws SQLException {
        try {
            Parent twoFARoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/TwoFA.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (currentStage != null) {
                Scene twoFAScene = new Scene(twoFARoot);
                currentStage.setScene(twoFAScene);
                currentStage.sizeToScene();
                currentStage.setResizable(false);
                currentStage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page 2FA : stage is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page 2FA : " + e.getMessage());
        }
    }

    private String parseRoleFromJson(String rolesJson) {
        try {
            JSONArray rolesArray = new JSONArray(rolesJson);
            if (rolesArray.length() > 0) {
                return rolesArray.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "default";
    }

    public void createAccountFormin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/register.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Register");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : " + e.getCause());
        }
    }

    public void resetAccountFormin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/User/reset-password.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Reset Password");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : " + e.getCause());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}