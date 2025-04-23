package entite;

public class UserTitle {
    private int userId;
    private int titleId;

    // Default constructor
    public UserTitle() {
    }

    // Constructor with all fields
    public UserTitle(int userId, int titleId) {
        this.userId = userId;
        this.titleId = titleId;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    @Override
    public String toString() {
        return "UserTitle{" +
                "userId=" + userId +
                ", titleId=" + titleId +
                '}';
    }
}