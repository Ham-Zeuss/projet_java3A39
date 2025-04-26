package Controller.Boubaker;

import entite.Pack;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import service.PackService;

import java.util.List;

public class MainBoubakerController {
    private final PackService packService = new PackService();

    @FXML
    private HBox packContainer;

    @FXML
    public void initialize() {
        System.out.println("MainBoubakerController: initialize() started");

        if (packContainer == null) {
            System.err.println("Error: packContainer is null");
            return;
        }

        packContainer.setAlignment(Pos.CENTER);
        packContainer.setSpacing(40);
        packContainer.setPadding(new Insets(10));
        packContainer.getChildren().clear(); // Clear existing cards

        try {
            List<Pack> packs = packService.getAllPacks();
            System.out.println("Loaded " + packs.size() + " packs from the database.");

            if (packs.isEmpty()) {
                System.out.println("No packs found in database, loading static cards.");
                loadStaticCards();
                return;
            }

            for (int i = 0; i < Math.min(packs.size(), 3); i++) {
                Pack pack = packs.get(i);
                String styleClass = i == 1 ? "pack-card premium-card" : "pack-card";
                VBox card = createCard(pack.getName(), pack.getPrice(), pack.getFeatures(), styleClass, pack.getName());
                packContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            System.err.println("Error loading packs: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Loading static cards due to error.");
            loadStaticCards();
        }
    }

    private void loadStaticCards() {
        packContainer.getChildren().addAll(
                createCard("Basic", 100.00, "Essential Features", "pack-card", "Basic"),
                createCard("Premium", 150.00, "Advanced Features", "pack-card premium-card", "Premium")
        );
    }

    private VBox createCard(String name, double price, String features, String styleClass, String buttonLabel) {
        VBox card = new VBox();
        card.getStyleClass().addAll(styleClass.split(" "));
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setSpacing(15);
        card.setMinWidth(250);
        card.setPrefWidth(250);
        card.setMaxWidth(300);

        Text nameText = new Text(name);
        nameText.getStyleClass().add("pack-name");
        if (styleClass.contains("premium-card")) {
            nameText.getStyleClass().add("premium-title");
        }

        Text priceText = new Text(String.format("$%.2f", price));
        priceText.getStyleClass().add("pack-price");

        Text featuresText = new Text(features);
        featuresText.getStyleClass().add("pack-features");
        featuresText.setWrappingWidth(200);

        Button button = new Button("Order Now");
        button.getStyleClass().add("order-button");
        button.setOnAction(event -> System.out.println("Ordered Pack: " + buttonLabel));

        card.getChildren().addAll(nameText, priceText, featuresText, button);
        return card;
    }
}