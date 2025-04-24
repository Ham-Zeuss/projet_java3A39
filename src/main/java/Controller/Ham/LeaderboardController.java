package Controller.Ham;

import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import service.UserService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LeaderboardController implements Initializable {

    @FXML private VBox leaderboardRoot;
    @FXML private Label errorLabel;
    @FXML private HBox top3Row;
    @FXML private VBox card1, card2, card3;
    @FXML private ImageView profilePic1, profilePic2, profilePic3;
    @FXML private Label name1, name2, name3;
    @FXML private Label score1, score2, score3;
    @FXML private Label title1, title2, title3;
    @FXML private Label message1, message2, message3;
    @FXML private VBox lowerRanksBox;

    private UserService userService;
    private final String DEFAULT_PROFILE_ICON = "/Images/default_avatar.png";
    private Label[][] cardLabels;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        cardLabels = new Label[][]{
                {name1, score1, title1, message1},
                {name2, score2, title2, message2},
                {name3, score3, title3, message3}
        };

        URL cssUrl = getClass().getResource("/css/leaderboard.css");
        if (cssUrl != null) {
            leaderboardRoot.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Leaderboard CSS not found!");
        }

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        try {
            List<User> topUsers = userService.getTopUsersByScore(10);
            if (topUsers == null || topUsers.isEmpty()) {
                errorLabel.setText("Leaderboard is currently empty.");
                top3Row.setVisible(false);
                lowerRanksBox.setVisible(false);
                return;
            }

            errorLabel.setText("");
            top3Row.setVisible(true);
            lowerRanksBox.setVisible(true);
            lowerRanksBox.getChildren().clear();
            lowerRanksBox.getChildren().add(createHeaderRow());

            // Populate Top 3
            if (topUsers.size() >= 1) {
                // 1st place (center)
                User first = topUsers.get(0);
                card1.getStyleClass().add("gold-border");
                Label[] firstLabels = cardLabels[0];
                firstLabels[0].setText(formatName(first.getPrenom(), first.getNom()));
                firstLabels[1].setText("Score: " + (first.getScoreTotal() != null ? first.getScoreTotal() : 0) + " Points");
                firstLabels[2].setText("Title: " + (first.getCurrentTitle() != null && first.getCurrentTitle().getName() != null ? first.getCurrentTitle().getName() : "No Title"));
                firstLabels[3].setText(getEncouragingMessage(1));
                card1.setVisible(true);
                setProfileImage(profilePic1, first.getPhoto());
            } else {
                card1.setVisible(false);
            }

            if (topUsers.size() >= 2) {
                // 2nd place (left)
                User second = topUsers.get(1);
                card2.getStyleClass().add("silver-border");
                Label[] secondLabels = cardLabels[1];
                secondLabels[0].setText(formatName(second.getPrenom(), second.getNom()));
                secondLabels[1].setText("Score: " + (second.getScoreTotal() != null ? second.getScoreTotal() : 0) + " Points");
                secondLabels[2].setText("Title: " + (second.getCurrentTitle() != null && second.getCurrentTitle().getName() != null ? second.getCurrentTitle().getName() : "No Title"));
                secondLabels[3].setText(getEncouragingMessage(2));
                card2.setVisible(true);
                setProfileImage(profilePic2, second.getPhoto());
            } else {
                card2.setVisible(false);
            }

            if (topUsers.size() >= 3) {
                // 3rd place (right)
                User third = topUsers.get(2);
                card3.getStyleClass().add("bronze-border");
                Label[] thirdLabels = cardLabels[2];
                thirdLabels[0].setText(formatName(third.getPrenom(), third.getNom()));
                thirdLabels[1].setText("Score: " + (third.getScoreTotal() != null ? third.getScoreTotal() : 0) + " Points");
                thirdLabels[2].setText("Title: " + (third.getCurrentTitle() != null && third.getCurrentTitle().getName() != null ? third.getCurrentTitle().getName() : "No Title"));
                thirdLabels[3].setText(getEncouragingMessage(3));
                card3.setVisible(true);
                setProfileImage(profilePic3, third.getPhoto());
            } else {
                card3.setVisible(false);
            }

            // Populate Ranks 4-10
            if (topUsers.size() > 3) {
                for (int i = 3; i < Math.min(topUsers.size(), 10); i++) {
                    User user = topUsers.get(i);
                    HBox row = createLeaderboardRow(user, i + 1);
                    lowerRanksBox.getChildren().add(row);
                }
            } else {
                lowerRanksBox.setVisible(false);
            }

        } catch (Exception e) {
            errorLabel.setText("Error loading leaderboard: " + e.getMessage());
            e.printStackTrace();
            top3Row.setVisible(false);
            lowerRanksBox.setVisible(false);
        }
    }

    private HBox createHeaderRow() {
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("leaderboard-header-row");

        Label rankHeader = createHeaderLabel("Rank", 180);
        Label userHeader = createHeaderLabel("User", 200);
        Label scoreHeader = createHeaderLabel("Score", 120);
        Label titleHeader = createHeaderLabel("Title", 150);
        Label messageHeader = createHeaderLabel("everyone is a Hero!", 150);

        headerRow.getChildren().addAll(rankHeader, userHeader, scoreHeader, titleHeader, messageHeader);
        return headerRow;
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("header-label");
        label.setMinWidth(width);
        label.setMaxWidth(width);
        return label;
    }

    private HBox createLeaderboardRow(User user, int rank) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("leaderboard-row");

        ImageView profilePic = new ImageView();
        profilePic.setFitHeight(40.0);
        profilePic.setFitWidth(40.0);
        profilePic.getStyleClass().add("profile-image-small");
        setProfileImage(profilePic, user.getPhoto());

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.getStyleClass().add("row-data");
        rankLabel.setMinWidth(60);
        rankLabel.setMaxWidth(60);

        Label nameLabel = new Label(formatName(user.getPrenom(), user.getNom()));
        nameLabel.getStyleClass().add("row-name");
        nameLabel.setMinWidth(200);
        nameLabel.setMaxWidth(200);

        Label scoreLabel = new Label("Score: " + (user.getScoreTotal() != null ? user.getScoreTotal() : 0) + " Points");
        scoreLabel.getStyleClass().add("row-data");
        scoreLabel.setMinWidth(120);
        scoreLabel.setMaxWidth(120);

        Label titleLabel = new Label("Title: " + (user.getCurrentTitle() != null && user.getCurrentTitle().getName() != null ? user.getCurrentTitle().getName() : "No Title"));
        titleLabel.getStyleClass().add("row-data");
        titleLabel.setMinWidth(150);
        titleLabel.setMaxWidth(150);

        Label messageLabel = new Label(getEncouragingMessage(rank));
        messageLabel.getStyleClass().add("row-data");
        messageLabel.setMinWidth(220);
        messageLabel.setMaxWidth(220);
        messageLabel.setWrapText(true);

        row.getChildren().addAll(profilePic, rankLabel, nameLabel, scoreLabel, titleLabel, messageLabel);
        HBox.setMargin(profilePic, new javafx.geometry.Insets(0, 10, 0, 0));

        return row;
    }

    private void setProfileImage(ImageView imageView, String imagePath) {
        try {
            Image img;
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("/")) {
                    URL imageUrl = getClass().getResource(imagePath);
                    img = new Image(imageUrl != null ? imageUrl.toExternalForm() : getClass().getResource(DEFAULT_PROFILE_ICON).toExternalForm());
                } else {
                    img = new Image(imagePath, true);
                }
            } else {
                img = new Image(getClass().getResource(DEFAULT_PROFILE_ICON).toExternalForm());
            }

            img.errorProperty().addListener((obs, old, isError) -> {

            });

            imageView.setImage(img);
            imageView.setClip(new javafx.scene.shape.Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, imageView.getFitWidth() / 2));

        } catch (Exception e) {
            System.err.println("Error setting profile image: " + e.getMessage());
            try {
                Image defaultImg = new Image(getClass().getResource(DEFAULT_PROFILE_ICON).toExternalForm());
                imageView.setImage(defaultImg);
                imageView.setClip(new javafx.scene.shape.Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, imageView.getFitWidth() / 2));
            } catch (Exception ex) {
                System.err.println("Failed to load default profile icon!");
                imageView.setImage(null);
            }
        }
    }

    private String getEncouragingMessage(int rank) {
        switch (rank) {
            case 1:
                return "Champion! You're the best!";
            case 2:
                return "So close to the top! Keep shining!";
            case 3:
                return "Bronze star! You're amazing!";
            case 4:
                return "Elite player! Push for the podium!";
            case 5:
                return "Fantastic effort! Stay strong!";
            case 6:
                return "Great job! Keep climbing!";
            case 7:
                return "You're in the top 10! Awesome!";
            case 8:
                return "Solid performance! Go for more!";
            case 9:
                return "Almost there! Keep it up!";
            case 10:
                return "Made the leaderboard! Proud of you!";
            default:
                return "Keep playing to rise!";
        }
    }

    private String formatName(String firstName, String lastName) {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}