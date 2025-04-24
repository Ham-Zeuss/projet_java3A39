package util;

public class SessionManager {
    private static int loggedInUserId = -1; // Default value indicates no user is logged in

    public static void setLoggedInUserId(int userId) {
        loggedInUserId = userId;
        System.out.println("Logged-in User ID set to: " + userId); // Debugging log
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void clearSession() {
        loggedInUserId = -1; // Reset to default value
        System.out.println("Session cleared. No user is logged in.");
    }
}