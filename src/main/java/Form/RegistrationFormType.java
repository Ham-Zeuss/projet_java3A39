package Form;

import javafx.scene.control.ToggleGroup;

import java.util.regex.Pattern;

public class RegistrationFormType {


    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\-]{4,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8,15}$");

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    public static boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            return age >= 6 && age <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidGouvernorat(String gouvernorat) {
        return gouvernorat != null && !gouvernorat.isEmpty();
    }

    public static boolean isValidPhotoPath(String path) {
        return path != null && !path.trim().isEmpty() &&
                (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg"));
    }

    public static boolean isRoleSelected(ToggleGroup toggleGroup) {
        return toggleGroup.getSelectedToggle() != null;
    }


}
