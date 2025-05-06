package Controller.Hedy;
import javafx.fxml.FXML;
import javafx.scene.shape.Path;
import javafx.scene.paint.Color;
import entite.Rating;

public class RatingController {

    @FXML
    private Path star1, star2, star3, star4, star5;  // These are the star buttons

    private int userId = 1; // Dummy user ID
    private int courseId = 101; // Dummy course ID

    // This method will be called when the user clicks a star
    @FXML
    private void setRating(int rating) {
        // Save the rating to the JSON file
        RatingsStorage.addOrUpdateRating(courseId, userId, rating);

        // Update the UI to show the selected rating visually
        updateStarDisplay(rating);
    }

    // Update the star display based on the selected rating
    private void updateStarDisplay(int rating) {
        star1.setFill(rating >= 1 ? Color.GOLD : Color.GRAY);
        star2.setFill(rating >= 2 ? Color.GOLD : Color.GRAY);
        star3.setFill(rating >= 3 ? Color.GOLD : Color.GRAY);
        star4.setFill(rating >= 4 ? Color.GOLD : Color.GRAY);
        star5.setFill(rating >= 5 ? Color.GOLD : Color.GRAY);
    }

    // Optionally, load the rating and update the stars when the page is loaded
    public void initialize() {
        // Retrieve rating from the JSON file (just an example for a course)
        int rating = RatingsStorage.getRatings().stream()
                .filter(r -> r.getCourseId() == courseId && r.getUserId() == userId)
                .map(Rating::getRating)
                .findFirst()
                .orElse(0);  // Default to 0 if no rating found

        updateStarDisplay(rating);
    }
}

