package entite;

public class Session {
    private static Session instance; // Singleton
    private int userId;
    private String email;

    private Session() {
        // Constructeur privé pour le singleton
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUser(int userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public void clearSession() {
        this.userId = 0;
        this.email = null;
        instance = null; // Réinitialiser la session
    }

    public boolean isActive() {
        return email != null;
    }
}