package Entity;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private String message;
    private LocalDateTime createdAt;

    // Constructeur par défaut
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur avec message
    public Notification(String message) {
        this();
        this.message = message;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Notification {" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}