package entite;

import java.time.LocalDateTime;

public class Consultation {
    private int id;
    private User userId; // Reference to the User entity (for user_id foreign key)
    private Profile profileId; // Reference to the Profile entity (for profile_id foreign key)
    private LocalDateTime consultationDate; // DATETIME maps to LocalDateTime
    private boolean isCompleted; // tinyint(1) maps to boolean

    // Default constructor
    public Consultation() {
    }

    // Constructor with all fields (including id)
    public Consultation(int id, User userId, Profile profileId, LocalDateTime consultationDate, boolean isCompleted) {
        this.id = id;
        this.userId = userId;
        this.profileId = profileId;
        this.consultationDate = consultationDate;
        this.isCompleted = isCompleted;
    }

    // Constructor without id (useful for creating new records)
    public Consultation(User userId, Profile profileId, LocalDateTime consultationDate, boolean isCompleted) {
        this.userId = userId;
        this.profileId = profileId;
        this.consultationDate = consultationDate;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Profile getProfileId() {
        return profileId;
    }

    public void setProfileId(Profile profileId) {
        this.profileId = profileId;
    }

    public LocalDateTime getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(LocalDateTime consultationDate) {
        this.consultationDate = consultationDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", userId=" + (userId != null ? userId.getNom() + " " + userId.getPrenom() : "null") +
                ", profileId=" + (profileId != null ? profileId.getSpecialite() : "null") +
                ", consultationDate=" + consultationDate +
                ", isCompleted=" + isCompleted +
                '}';
    }
}