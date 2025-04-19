package test;

import entite.Profile;
import entite.Title;
import entite.User;
import service.ProfileService;
import service.TitleService;
import service.UserService;

public class ProfileMain {
    public static void main(String[] args) {
        // First, create a Title to associate with the User
        TitleService titleService = new TitleService();
        Title title = new Title("Profile Title", 300, 100);
        titleService.createPst(title);

        // Fetch the created Title
        Title createdTitle = titleService.readAll().get(0);

        // Create a User to associate with the Profile
        UserService userService = new UserService();
        User user = new User(
                createdTitle, // currentTitle
                "Smith", // nom
                "Alice", // prenom
                "alice.smith@example.com", // email
                "[\"ROLE_USER\"]", // roles (JSON)
                "hashedpassword456", // password
                true, // isVerified
                35, // age
                "Sfax", // gouvernorat
                150, // points
                "987654321", // numero
                null, // enfantId
                "profile_picture.jpg", // photo
                "active", // status
                750, // scoreTotal
                true, // isActive
                100.50, // balance
                "[\"feature3\", \"feature4\"]", // featuresUnlocked (JSON)
                "totpsecret456" // totpSecret
        );
        userService.createPst(user);

        // Fetch the created User
        User createdUser = userService.readAll().get(0);

        // Create a new Profile object
        Profile profile = new Profile(
                createdUser, // userId
                "Experienced professional with 10 years in the field.", // biographie
                "Cardiology", // specialite
                "Books, Videos, Articles", // ressources
                120.50, // prixConsultation
                36.806494, // latitude
                10.181532 // longitude
        );

        // Instantiate the ProfileService
        ProfileService ps = new ProfileService();

        // Test CRUD operations
        // 1. Create a new record
        System.out.println("Creating a new profile...");
        ps.createPst(profile);

        // 2. Read all profiles
        System.out.println("\nReading all profiles:");
        ps.readAll().forEach(System.out::println);

        // 3. Update the profile (using the first record for demo)
        System.out.println("\nUpdating the first profile...");
        Profile profileToUpdate = ps.readAll().get(0); // Get the first profile
        profileToUpdate.setBiographie(null); // Test nullable field
        profileToUpdate.setSpecialite("Neurology");
        profileToUpdate.setRessources("Online Courses, Webinars");
        profileToUpdate.setPrixConsultation(150.75);
        profileToUpdate.setLatitude(null); // Test nullable field
        profileToUpdate.setLongitude(null); // Test nullable field
        ps.update(profileToUpdate);

        // 4. Read all profiles again to see the update
        System.out.println("\nReading all profiles after update:");
        ps.readAll().forEach(System.out::println);

        // 5. Delete the profile
        //System.out.println("\nDeleting the profile...");
        //ps.delete(profileToUpdate);

        // 6. Read all profiles to confirm deletion
        //System.out.println("\nReading all profiles after deletion:");
        //ps.readAll().forEach(System.out::println);

        // 7. Test readById (assuming ID 1 exists, adjust as needed)
        System.out.println("\nReading profile with ID 1:");
        Profile profileById = ps.readById(1);
        System.out.println(profileById != null ? profileById : "Profile with ID 1 not found.");
    }
}