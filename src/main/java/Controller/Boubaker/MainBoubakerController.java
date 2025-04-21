package Controller.Boubaker;

import entite.Pack;
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
    private VBox root;

    public VBox getRoot() {
        if (root == null) {
            initialize();
        }
        return root;
    }

    private void initialize() {
        root = new VBox();
        root.getStyleClass().add("main-container");
        root.setAlignment(Pos.TOP_CENTER);
        root.setSpacing(30);
        root.setPadding(new Insets(20));

        Text title = new Text("Our Packs");
        title.getStyleClass().add("main-title");

        HBox packContainer = new HBox();
        packContainer.getStyleClass().add("pack-container");
        packContainer.setAlignment(Pos.CENTER);
        packContainer.setSpacing(40);
        packContainer.setPadding(new Insets(10));

        try {
            List<Pack> packs = packService.getAllPacks();
            System.out.println("Loaded packs: " + packs.size());

            if (packs.isEmpty()) {
                throw new Exception("No packs found in database");
            }

            // Create cards for available packs
            for (int i = 0; i < Math.min(packs.size(), 3); i++) { // Show max 3 packs
                Pack pack = packs.get(i);
                VBox card = createCard(
                        pack.getName(),
                        pack.getPrice(),
                        pack.getFeatures(),
                        i == 1 ? "pack-card premium-card" : "pack-card" // Make middle card premium
                );
                packContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            System.err.println("Error loading packs: " + e.getMessage());
            // Default fallback cards
            packContainer.getChildren().addAll(
                    createCard("Basic", 100.00, "Essential Features", "pack-card"),
                    createCard("Premium", 150.00, "Advanced Features", "pack-card premium-card")
            );
        }

        root.getChildren().addAll(title, packContainer);
    }

    private VBox createCard(String name, double price, String features, String styleClass) {
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

        card.getChildren().addAll(nameText, priceText, featuresText, button);
        return card;
    }
}