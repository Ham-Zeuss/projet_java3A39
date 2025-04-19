package Controller;

import entite.Title;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import service.TitleService;
import service.UserService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserTitlesController implements Initializable {

    @FXML
    private GridPane titlesGrid;

    @FXML
    private Label feedbackLabel;

    private TitleService titleService;
    private UserService userService;
    private static final int BUYER_ID = 14;
    private static final int COLUMNS = 3; // Number of cards per row

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleService = new TitleService();
        userService = new UserService();
        loadUserTitles();
    }

    private void loadUserTitles() {
        titlesGrid.getChildren().clear();
        List<Title> titles = titleService.readByUserId(BUYER_ID);
        if (titles.isEmpty()) {
            feedbackLabel.setText("No titles owned.");
            feedbackLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        for (int i = 0; i < titles.size(); i++) {
            Title title = titles.get(i);
            VBox card = createTitleCard(title);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            titlesGrid.add(card, col, row);
        }
    }

    private VBox createTitleCard(Title title) {
        VBox card = new VBox();
        card.getStyleClass().add("title-card");
        card.setPadding(new Insets(10));
        card.setSpacing(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        Label nameLabel = new Label(title.getName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        card.getChildren().add(nameLabel);
        card.setOnMouseClicked(event -> {
            try {
                userService.updateCurrentTitleId(BUYER_ID, title.getId());
                feedbackLabel.setText("Title '" + title.getName() + "' set successfully!");
                feedbackLabel.setStyle("-fx-text-fill: green;");
            } catch (Exception e) {
                feedbackLabel.setText("Failed to set title: " + e.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: red;");
            }
        });

        return card;
    }
}