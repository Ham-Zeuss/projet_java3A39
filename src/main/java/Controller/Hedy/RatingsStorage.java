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
        ratings.clear(); // Clear old data before reload
        try (Reader reader = new FileReader(FILE_PATH)) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            JsonArray ratingsArray = jsonObject.getAsJsonArray("ratings");

            if (ratingsArray != null) {
                for (JsonElement element : ratingsArray) {
                    ratings.add(new Gson().fromJson(element, Rating.class));
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            System.out.println("No valid ratings found. Starting fresh.");
        }
    }

    // Save ratings to the JSON file
    public static void saveRatings() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) file.createNewFile(); // Create file if missing

            try (Writer writer = new FileWriter(FILE_PATH)) {
                Gson gson = new Gson();
                JsonObject jsonObject = new JsonObject();
                JsonArray ratingsArray = new JsonArray();

                for (Rating rating : ratings) {
                    ratingsArray.add(gson.toJsonTree(rating));
                }

                jsonObject.add("ratings", ratingsArray);
                gson.toJson(jsonObject, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a rating only if it doesn't already exist
    public static boolean addRatingIfNotExists(int courseId, int userId, int rating) {
        // Check if user has already rated this course
        boolean alreadyRated = ratings.stream()
                .anyMatch(r -> r.getCourseId() == courseId && r.getUserId() == userId);

        if (alreadyRated) {
            return false; // Already rated, do not allow again
        }

        // Add new rating
        ratings.add(new Rating(courseId, userId, rating));
        saveRatings();
        return true;
    }

    // Get all stored ratings
    public static List<Rating> getRatings() {
        return ratings;
    }
}