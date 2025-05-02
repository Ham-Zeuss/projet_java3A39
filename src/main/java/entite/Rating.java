package entite;

public class Rating {
    private int courseId;
    private int userId;
    private int rating;

    public Rating(int courseId, int userId, int rating) {
        this.courseId = courseId;
        this.userId = userId;
        this.rating = rating;
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
