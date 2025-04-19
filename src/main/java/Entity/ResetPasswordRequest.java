package Entity;

import java.time.LocalDateTime;

public class ResetPasswordRequest {
    private int id;
    private User user;
    private LocalDateTime expiresAt;
    private String selector;
    private String hashedToken;

    public ResetPasswordRequest(User user, LocalDateTime expiresAt, String selector, String hashedToken) {
        this.user = user;
        this.expiresAt = expiresAt;
        this.selector = selector;
        this.hashedToken = hashedToken;
    }

    // Getters
    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getSelector() {
        return selector;
    }

    public String getHashedToken() {
        return hashedToken;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public void setHashedToken(String hashedToken) {
        this.hashedToken = hashedToken;
    }

    // Utility method to check if request is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "id=" + id +
                ", userId=" + user.getId() +
                ", expiresAt=" + expiresAt +
                ", selector='" + selector + '\'' +
                '}';
    }
}