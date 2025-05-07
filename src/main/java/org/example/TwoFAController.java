package org.example;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import entite.Session;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.TwoFactorAuthService;
import test.Sidebar;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TwoFAController {

    @FXML
    private ImageView qrCodeImage;
    @FXML
    private TextField codeInput;
    @FXML
    private Label feedbackText;

    private String secretKey;
    private static final String TEXT_COLOR_DARK = "#333333";

    public void initialize() {
        TwoFactorAuthService authService = new TwoFactorAuthService();
        GoogleAuthenticatorKey key = authService.generateSecretKey();
        secretKey = key.getKey();

        try {
            String otpURL = "otpauth://totp/MonAppEducative:user@example.com?secret=" + secretKey + "&issuer=MonAppEducative";
            BitMatrix matrix = new MultiFormatWriter().encode(otpURL, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            qrCodeImage.setImage(fxImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void verifyCode(ActionEvent event) {
        try {
            int code = Integer.parseInt(codeInput.getText());
            TwoFactorAuthService authService = new TwoFactorAuthService();

            boolean isValid = authService.verifyCode(secretKey, code);
            feedbackText.setText(isValid ? "✅ Code valide" : "❌ Code invalide");

            if (isValid) {
                Session session = Session.getInstance();
                String role = session.getRole();

                if ("ROLE_ADMIN".equals(role)) {
                    loadDashboardLayout(event);
                    return;
                }

                Map<String, String> roleToFxmlMap = new HashMap<>();
                roleToFxmlMap.put("ROLE_MEDECIN", "/MaryemFXML/FrontDoctorsDisplayProfiles.fxml");
                roleToFxmlMap.put("ROLE_ENSEIGNANT", "/HedyFXML/AffichageModule.fxml");
                roleToFxmlMap.put("ROLE_PARENT", "/User/Home.fxml");

                String defaultFxml = "/User/Home.fxml";
                String fxmlPath = roleToFxmlMap.getOrDefault(role, defaultFxml);

                VBox mainContent = new VBox();
                mainContent.setAlignment(Pos.TOP_CENTER);

                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(1920);
                    headerImageView.setSmooth(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                    mainContent.getChildren().add(headerImageView);
                } catch (Exception e) {
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    mainContent.getChildren().add(errorLabel);
                }

                URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    throw new Exception("FXML file not found at path: " + fxmlPath);
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent bodyContent = loader.load();

                if ("ROLE_MEDECIN".equals(role)) {
                    bodyContent.setStyle("-fx-background-color: #B8DAB8FF; -fx-pref-width: 1920; -fx-pref-height: 1080;");
                }

                if ("ROLE_PARENT".equals(role)) {
                    FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                    VBox headerFxmlContent = headerFxmlLoader.load();
                    headerFxmlContent.setPrefSize(1000, 100);
                    mainContent.getChildren().add(headerFxmlContent);

                    HomeController homeController = loader.getController();
                    if (homeController != null) {
                        homeController.setWelcomeMessage("Bienvenue ID: " + session.getUserId());
                    }
                }

                mainContent.getChildren().add(bodyContent);

                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(1920);
                    footerImageView.setSmooth(true);
                    mainContent.getChildren().add(footerImageView);
                } catch (Exception e) {
                    Label errorLabel = new Label("Footer image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    mainContent.getChildren().add(errorLabel);
                }

                ScrollPane scrollPane = new ScrollPane(mainContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                Scene scene = new Scene(scrollPane, 1920, 1080);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/css/UserTitlesStyle.css").toExternalForm());

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Dashboard - " + role);
                currentStage.setFullScreen(false);
                currentStage.setWidth(1920);
                currentStage.setHeight(1080);
                currentStage.setX(0); // Start at top-left
                currentStage.setY(0); // Start at top-left
                currentStage.setResizable(true);
                currentStage.show();
            }
        } catch (NumberFormatException e) {
            feedbackText.setText("❌ Veuillez entrer un code numérique valide");
        } catch (Exception e) {
            e.printStackTrace();
            feedbackText.setText("❌ Erreur lors du chargement de la page");
        }
    }

    private void loadDashboardLayout(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Consumer<String> loadFXMLConsumer = fxmlPath -> {
            try {
                loadFXML(currentStage, fxmlPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                currentStage,
                () -> {
                    try {
                        loadDashboard(currentStage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    try {
                        loadFXML(currentStage, "/User/index_user.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    try {
                        loadFXML(currentStage, "/HamzaFXML/ListPexelWords.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> System.out.println("Logout clicked (implement logout logic here)"),
                loadFXMLConsumer
        );

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");
        root.setLeft(sidebar);
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        currentStage.setScene(scene);
        currentStage.setTitle("Admin Dashboard");
        currentStage.setFullScreen(false);
        currentStage.setWidth(1920);
        currentStage.setHeight(1080);
        currentStage.setX(0); // Start at top-left
        currentStage.setY(0); // Start at top-left
        currentStage.setResizable(true);
        currentStage.show();
    }

    private void loadDashboard(Stage stage) throws IOException {
        Consumer<String> loadFXMLConsumer = fxmlPath -> {
            try {
                loadFXML(stage, fxmlPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");

        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                stage,
                () -> {
                    try {
                        loadDashboard(stage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    try {
                        loadFXML(stage, "/User/index_user.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    try {
                        loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> System.out.println("Logout clicked (implement logout logic here)"),
                loadFXMLConsumer
        );

        root.setLeft(sidebar);
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.setFullScreen(false);
        stage.setWidth(1920);
        stage.setHeight(1080);
        stage.setX(0); // Start at top-left
        stage.setY(0); // Start at top-left
        stage.setResizable(true);
        stage.show();
    }

    void loadFXML(Stage stage, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent fxmlContent = loader.load();

        Consumer<String> loadFXMLConsumer = path -> {
            try {
                loadFXML(stage, path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F7F7F7;");

        Sidebar sidebarCreator = new Sidebar();
        ScrollPane sidebar = sidebarCreator.createSidebar(
                stage,
                () -> {
                    try {
                        loadDashboard(stage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    try {
                        loadFXML(stage, "/User/index_user.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    try {
                        loadFXML(stage, "/HamzaFXML/ListPexelWords.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                () -> System.out.println("Logout clicked (implement logout logic here)"),
                loadFXMLConsumer
        );

        root.setLeft(sidebar);
        root.setCenter(fxmlContent);

        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/dashboard-sidebar.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.setFullScreen(false);
        stage.setWidth(1920);
        stage.setHeight(1080);
        stage.setX(0); // Start at top-left
        stage.setY(0); // Start at top-left
        stage.setResizable(true);
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

    @FXML
    private void annnulerButtonClicked(ActionEvent event) {
        Stage stage = (Stage) qrCodeImage.getScene().getWindow();
        stage.close();
    }
}