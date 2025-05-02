package Controller.Hedy;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import entite.Rating;
public class RatingsStorage {

    private static final String FILE_PATH = "ratings.json"; // Path to the JSON file
    private static List<Rating> ratings = new ArrayList<>();

    static {
        // Load the ratings from the JSON file when the class is first accessed
        loadRatings();
    }

    // Load ratings from the JSON file
    public static void loadRatings() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray ratingsArray = jsonObject.getAsJsonArray("ratings");

            for (JsonElement element : ratingsArray) {
                Rating rating = gson.fromJson(element, Rating.class);
                ratings.add(rating);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save ratings to the JSON file
    public static void saveRatings() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            JsonArray ratingsArray = new JsonArray();

            for (Rating rating : ratings) {
                JsonElement ratingElement = gson.toJsonTree(rating);
                ratingsArray.add(ratingElement);
            }

            jsonObject.add("ratings", ratingsArray);
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add or update a rating
    public static void addOrUpdateRating(int courseId, int userId, int rating) {
        // Check if a rating already exists for the given courseId and userId
        for (Rating r : ratings) {
            if (r.getCourseId() == courseId && r.getUserId() == userId) {
                r.setRating(rating); // Update the rating if it exists
                saveRatings();
                return;
            }
        }
        // If no existing rating is found, add a new one
        ratings.add(new Rating(courseId, userId, rating));
        saveRatings();
    }

    // Optionally, get all ratings
    public static List<Rating> getRatings() {
        return ratings;
    }
}

