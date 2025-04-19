package test;

import entite.Title;
import entite.User;
import service.TitleService;
import service.UserService;

public class UserMain {
    public static void main(String[] args) {
        // First, create a Title to associate with the User
        TitleService titleService = new TitleService();
        Title title = new Title("User Title", 200, 75);
        titleService.createPst(title);

        // Fetch the created Title (assuming it's the first one for simplicity)
        Title createdTitle = titleService.readAll().get(0);

        // Create a new User object
        User user = new User(
                createdTitle, // currentTitle
                "Doe", // nom
                "John", // prenom
                "john.doe@example.com", // email
                "[\"ROLE_USER\"]", // roles (JSON)
                "hashedpassword123", // password
                true, // isVerified
                30, // age
                "Tunis", // gouvernorat
                100, // points
                "123456789", // numero
                null, // enfantId
                "profile.jpg", // photo
                "active", // status
                500, // scoreTotal
                true, // isActive
                50.75, // balance
                "[\"feature1\", \"feature2\"]", // featuresUnlocked (JSON)
                "totpsecret123" // totpSecret
        );

        // Instantiate the UserService
        UserService us = new UserService();

        // Test CRUD operations
        // 1. Create a new record
        System.out.println("Creating a new user...");
        us.createPst(user);

        // 2. Read all users
        System.out.println("\nReading all users:");
        us.readAll().forEach(System.out::println);

        // 3. Update the user (using the first record for demo)
        System.out.println("\nUpdating the first user...");
        User userToUpdate = us.readAll().get(0); // Get the first user
        userToUpdate.setNom("Smith");
        userToUpdate.setPrenom("Jane");
        userToUpdate.setEmail("jane.smith@example.com");
        userToUpdate.setPoints(null); // Test nullable field
        userToUpdate.setStatus(null); // Test nullable field
        userToUpdate.setBalance(75.25);
        userToUpdate.setFeaturesUnlocked(null); // Test nullable field
        us.update(userToUpdate);

        // 4. Read all users again to see the update
        System.out.println("\nReading all users after update:");
        us.readAll().forEach(System.out::println);

        // 5. Delete the user
        //System.out.println("\nDeleting the user...");
        //us.delete(userToUpdate);

        // 6. Read all users to confirm deletion
        System.out.println("\nReading all users after deletion:");
        us.readAll().forEach(System.out::println);

        // 7. Test readById (assuming ID 1 exists, adjust as needed)
        System.out.println("\nReading user with ID 1:");
        User userById = us.readById(1);
        System.out.println(userById != null ? userById : "User with ID 1 not found.");
    }
}