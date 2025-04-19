package Controller;

import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import service.UserService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LeaderboardController implements Initializable {

    @FXML
    private HBox row1, row2, row3, row4;

    @FXML
    private Label name1, score1, title1, message1;
    @FXML
    private Label name2, score2, title2, message2;
    @FXML
    private Label name3, score3, title3, message3;
    @FXML
    private Label name4, score4, title4, message4;
    @FXML
    private Label name5, score5, title5, message5;
    @FXML
    private Label name6, score6, title6, message6;
    @FXML
    private Label name7, score7, title7, message7;
    @FXML
    private Label name8, score8, title8, message8;
    @FXML
    private Label name9, score9, title9, message9;
    @FXML
    private Label name10, score10, title10, message10;

    @FXML
    private Label errorLabel;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        try {
            List<User> topUsers = userService.getTopUsersByScore(10);
            Label[][] cardLabels = {
                    {name1, score1, title1, message1},
                    {name2, score2, title2, message2},
                    {name3, score3, title3, message3},
                    {name4, score4, title4, message4},
                    {name5, score5, title5, message5},
                    {name6, score6, title6, message6},
                    {name7, score7, title7, message7},
                    {name8, score8, title8, message8},
                    {name9, score9, title9, message9},
                    {name10, score10, title10, message10}
            };

            if (topUsers.isEmpty()) {
                errorLabel.setText("No users found with scores.");
                row1.setVisible(false);
                row2.setVisible(false);
                row3.setVisible(false);
                row4.setVisible(false);
                return;
            }

            for (int i = 0; i < Math.min(topUsers.size(), 10); i++) {
                User user = topUsers.get(i);
                Label[] labels = cardLabels[i];
                labels[0].setText((user.getPrenom() != null ? user.getPrenom() : "") + " " + (user.getNom() != null ? user.getNom() : ""));
                labels[1].setText("Score: " + (user.getScoreTotal() != null ? user.getScoreTotal() : 0));
                labels[2].setText("Title: " + (user.getCurrentTitle() != null && user.getCurrentTitle().getName() != null ? user.getCurrentTitle().getName() : "No Title"));
                labels[3].setText(getEncouragingMessage(i + 1));
                labels[0].getParent().setVisible(true);
            }

            // Hide empty cards
            for (int i = topUsers.size(); i < 10; i++) {
                cardLabels[i][0].getParent().setVisible(false);
            }

            // Show/hide rows based on user count
            row1.setVisible(topUsers.size() >= 1);
            row2.setVisible(topUsers.size() >= 2);
            row3.setVisible(topUsers.size() >= 4);
            row4.setVisible(topUsers.size() >= 7);

        } catch (Exception e) {
            errorLabel.setText("Error loading leaderboard: " + e.getMessage());
            row1.setVisible(false);
            row2.setVisible(false);
            row3.setVisible(false);
            row4.setVisible(false);
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
}