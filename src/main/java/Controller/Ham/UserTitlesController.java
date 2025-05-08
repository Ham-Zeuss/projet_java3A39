package Controller.Ham;

import entite.Session;
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
    private static final int COLUMNS = 3; // Number of cards per row

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleService = new TitleService();
        userService = new UserService();
        loadUserTitles();
    }

    private void loadUserTitles() {
        titlesGrid.getChildren().clear();
        Session session = Session.getInstance();
        int userId = session.getUserId();

        if (!session.isActive() || userId == 0) {
            feedbackLabel.setText("Please log in to view your titles.");
            feedbackLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        List<Title> titles;
        try {
            titles = titleService.readByUserId(userId);
        } catch (Exception e) {
            feedbackLabel.setText("Error loading titles: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (titles.isEmpty()) {
            feedbackLabel.setText("No titles owned.");
            feedbackLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        for (int i = 0; i < titles.size(); i++) {
            Title title = titles.get(i);
            VBox card = createTitleCard(title, userId);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            titlesGrid.add(card, col, row);
        }
    }

    private VBox createTitleCard(Title title, int userId) {
        VBox card = new VBox();
        card.getStyleClass().add("pack-card");
        card.setPadding(new Insets(10));
        card.setSpacing(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        Label nameLabel = new Label(title.getName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        card.getChildren().add(nameLabel);
        card.setOnMouseClicked(event -> {
            try {
                userService.updateCurrentTitleId(userId, title.getId());
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