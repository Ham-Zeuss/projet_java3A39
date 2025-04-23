package test;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
public class Sidebar {

    private static final String TEXT_COLOR_DARK = "#333333";
    private static final String TEXT_COLOR_WHITE = "#FFFFFF";
    private static final String SELECTED_ITEM_COLOR = "#C20114";
    private final List<HBox> sidebarButtons = new ArrayList<>();
    private static final Map<String, Image> ICON_CACHE = new HashMap<>();

    public ScrollPane createSidebar(Stage stage, Runnable dashboardAction, Runnable utilisateursAction, Runnable pixelWordsAction, Runnable logoutAction, java.util.function.Consumer<String> loadFXML){
        // Preload images into cache
        ICON_CACHE.put("dashboard", new Image("https://img.icons8.com/?size=100&id=91234&format=png&color=000000"));
        ICON_CACHE.put("utilisateurs", new Image("https://img.icons8.com/?size=100&id=109466&format=png&color=000000"));
        ICON_CACHE.put("paiement", new Image("https://img.icons8.com/?size=100&id=112793&format=png&color=000000"));
        ICON_CACHE.put("docteurs", new Image("https://img.icons8.com/?size=100&id=123630&format=png&color=000000"));
        ICON_CACHE.put("consultation", new Image("https://img.icons8.com/?size=100&id=DzVh6MRkAbVz&format=png&color=000000"));
        ICON_CACHE.put("commentaire", new Image("https://img.icons8.com/?size=100&id=111133&format=png&color=000000"));
        ICON_CACHE.put("module", new Image("https://img.icons8.com/?size=100&id=J2t_uKtMD3D7&format=png&color=000000"));
        ICON_CACHE.put("quiz", new Image("https://img.icons8.com/?size=100&id=113080&format=png&color=000000"));
        ICON_CACHE.put("store-items", new Image("https://img.icons8.com/?size=100&id=91197&format=png&color=000000"));
        ICON_CACHE.put("titles", new Image("https://img.icons8.com/?size=100&id=64013&format=png&color=000000"));
        ICON_CACHE.put("pixel-words", new Image("https://img.icons8.com/?size=100&id=111401&format=png&color=000000"));
        ICON_CACHE.put("logout", new Image("https://img.icons8.com/?size=100&id=110469&format=png&color=000000"));
        ICON_CACHE.put("expand-arrow", new Image("https://img.icons8.com/ios-filled/50/000000/expand-arrow--v1.png"));

        // Load the custom font
        Font.loadFont(getClass().getResource("/Fonts/BubblegumSans-Regular.ttf").toExternalForm(), 14);

        // Sidebar content
        VBox sidebarContent = new VBox(10);
        sidebarContent.setPadding(new Insets(20));
        sidebarContent.setAlignment(Pos.TOP_LEFT);

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(20, 40, 20, 20));
        logoBox.setStyle("-fx-background-color: transparent; -fx-effect: none;");

        ImageView logoImage = new ImageView(new Image(getClass().getResourceAsStream("/Images/kidslogo.png")));
        logoImage.setFitHeight(120);
        logoImage.setFitWidth(120);
        logoImage.setPreserveRatio(true);
        logoImage.setStyle("-fx-background-color: #F5F5F5;");

        Label logoText = new Label("");
        logoText.setFont(Font.font("Bubblegum Sans", FontWeight.BOLD, 25));
        logoText.setTextFill(javafx.scene.paint.Color.web("#C20114"));

        logoBox.getChildren().addAll(logoImage, logoText);

        // Menu Section
        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        menuLabel.setTextFill(javafx.scene.paint.Color.web(TEXT_COLOR_DARK));
        menuLabel.setPadding(new Insets(10, 0, 5, 0));

        // Sidebar buttons
        HBox dashboardBtn = createSidebarItemWithDropdown("Dashboard", "dashboard", stage, dashboardAction);
        HBox analyticsBtn = createSidebarItem("Utilisateurs", "utilisateurs", stage, utilisateursAction);
        HBox ecommerceBtn = createSidebarItem("Paiement", "paiement", stage, () -> {
            System.out.println("Paiement clicked (no navigation implemented)");
        });

        // Application Section
        Label appLabel = new Label("Medecins");
        appLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        appLabel.setTextFill(javafx.scene.paint.Color.web(TEXT_COLOR_DARK));
        appLabel.setPadding(new Insets(20, 0, 5, 0));

        HBox chatBtn = createSidebarItem("Docteurs", "docteurs", stage, () -> loadFXML.accept("/MaryemFXML/DisplayProfiles.fxml"));
        HBox emailBtn = createSidebarItem("Consultation", "consultation", stage, () ->loadFXML.accept("/MaryemFXML/DisplayConsultations.fxml"));
        HBox kanbanBtn = createSidebarItem("Commentaire", "commentaire", stage, () -> loadFXML.accept("/MaryemFXML/ReportedComments.fxml"));
        // Pages Section
        Label pagesLabel = new Label("Contenu");
        pagesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        pagesLabel.setTextFill(javafx.scene.paint.Color.web(TEXT_COLOR_DARK));
        appLabel.setPadding(new Insets(20, 0, 5, 0));

        HBox authBtn = createSidebarItem("Module", "module", stage, () -> {
            System.out.println("Module clicked (no navigation implemented)");
        });
        HBox utilityBtn = createSidebarItem("Quiz", "quiz", stage, () -> {
            System.out.println("Quiz clicked (no navigation implemented)");
        });

        // Elements Section
        Label elementsLabel = new Label("Store");
        elementsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        elementsLabel.setTextFill(javafx.scene.paint.Color.web(TEXT_COLOR_DARK));
        elementsLabel.setPadding(new Insets(20, 0, 5, 0));

        HBox storeBtn = createSidebarItem("Store Items", "store-items", stage, () -> {
            System.out.println("Store Items clicked (no navigation implemented)");
        });
        HBox titlesBtn = createSidebarItem("Titles", "titles", stage, () -> {
            System.out.println("Titles clicked (no navigation implemented)");
        });
        HBox pixelWordsBtn = createSidebarItem("Pixel Words", "pixel-words", stage, pixelWordsAction);

        // Logout Button
        HBox logoutBtn = createSidebarItem("Logout", "logout", stage, pixelWordsAction);

        // Add buttons to tracking list
        sidebarButtons.addAll(List.of(dashboardBtn, analyticsBtn, ecommerceBtn, chatBtn, emailBtn, kanbanBtn, authBtn, utilityBtn, storeBtn, titlesBtn, pixelWordsBtn, logoutBtn));

        // Spacer to push logout button to the bottom
        VBox spacer = new VBox();
        spacer.setPrefHeight(0);
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        sidebarContent.getChildren().addAll(
                logoBox, menuLabel, dashboardBtn, analyticsBtn, ecommerceBtn,
                appLabel, chatBtn, emailBtn, kanbanBtn,
                pagesLabel, authBtn, utilityBtn,
                elementsLabel, storeBtn, titlesBtn, pixelWordsBtn,
                spacer, logoutBtn
        );

        // Wrap in ScrollPane
        ScrollPane sidebar = new ScrollPane(sidebarContent);
        sidebar.setFitToWidth(true);
        sidebar.setStyle("-fx-background-color: #F5F5F5; -fx-min-width: 300px; -fx-max-width: 300px; -fx-border-color: transparent #E0E0E0 transparent transparent; -fx-border-width: 1px;");
        sidebar.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return sidebar;
    }

    private HBox createSidebarItem(String text, String iconKey, Stage stage, Runnable action) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10, 15, 10, 15));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefHeight(50);
        item.getStyleClass().add("sidebar-item");

        ImageView icon = new ImageView(ICON_CACHE.get(iconKey));
        icon.setFitHeight(50);
        icon.setFitWidth(50);

        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        label.setPrefWidth(200);

        item.getChildren().addAll(icon, label);

        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: " + SELECTED_ITEM_COLOR + "; -fx-text-fill: " + TEXT_COLOR_WHITE + "; -fx-background-radius: 5;");
        });
        item.setOnMouseExited(e -> {
            if (!item.getStyleClass().contains("selected")) {
                item.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR_DARK + "; -fx-background-radius: 5;");
            }
        });
        item.setOnMouseClicked(e -> {
            // Clear selection from other buttons
            sidebarButtons.forEach(btn -> {
                btn.getStyleClass().remove("selected");
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR_DARK + "; -fx-background-radius: 5;");
            });
            // Select current button
            item.getStyleClass().add("selected");
            action.run();
        });

        return item;
    }

    private HBox createSidebarItemWithDropdown(String text, String iconKey, Stage stage, Runnable action) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10, 15, 10, 15));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefHeight(50);
        item.getStyleClass().add("sidebar-item");

        ImageView icon = new ImageView(ICON_CACHE.get(iconKey));
        icon.setFitHeight(50);
        icon.setFitWidth(50);

        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        label.setPrefWidth(150);

        ImageView dropdownIcon = new ImageView(ICON_CACHE.get("expand-arrow"));
        dropdownIcon.setFitHeight(12);
        dropdownIcon.setFitWidth(12);
        HBox.setMargin(dropdownIcon, new Insets(0, 0, 0, 30));

        item.getChildren().addAll(icon, label, dropdownIcon);

        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: " + SELECTED_ITEM_COLOR + "; -fx-text-fill: " + TEXT_COLOR_WHITE + "; -fx-background-radius: 5;");
        });
        item.setOnMouseExited(e -> {
            if (!item.getStyleClass().contains("selected")) {
                item.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR_DARK + "; -fx-background-radius: 5;");
            }
        });
        item.setOnMouseClicked(e -> {
            // Clear selection from other buttons
            sidebarButtons.forEach(btn -> {
                btn.getStyleClass().remove("selected");
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR_DARK + "; -fx-background-radius: 5;");
            });
            // Select current button
            item.getStyleClass().add("selected");
            action.run();
        });

        return item;
    }

    private HBox createSidebarItemWithTag(String text, String iconKey, Stage stage, Runnable action) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10, 15, 10, 15));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefHeight(50);
        item.getStyleClass().add("sidebar-item");

        ImageView icon = new ImageView(ICON_CACHE.get(iconKey));
        icon.setFitHeight(50);
        icon.setFitWidth(50);

        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        label.setPrefWidth(150);

        Label tag = new Label("NEW");
        tag.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        tag.setTextFill(javafx.scene.paint.Color.WHITE);
        tag.setStyle("-fx-background-color: #C20114; -fx-background-radius: 10; -fx-padding: 2 5;");
        HBox.setMargin(tag, new Insets(0, 0, 0, 20));

        item.getChildren().addAll(icon, label, tag);

        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: " + SELECTED_ITEM_COLOR + "; -fx-text-fill: " + TEXT_COLOR_WHITE + "; -fx-background-radius: 5;");
        });
        item.setOnMouseExited(e -> {
            if (!item.getStyleClass().contains("selected")) {
                item.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR_DARK + "; -fx-background-radius: 5;");
            }
        });
        item.setOnMouseClicked(e -> {
            // Clear selection from other buttons
            sidebarButtons.forEach(btn -> {
                btn.getStyleClass().remove("selected");
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR_DARK + "; -fx-background-radius: 5;");
            });
            // Select current button
            item.getStyleClass().add("selected");
            action.run();
        });

        return item;
    }
}
