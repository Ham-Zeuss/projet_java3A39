package test;

import entite.Consultation;
import entite.Profile;
import entite.Title;
import entite.User;
import service.ConsultationService;
import service.ProfileService;
import service.TitleService;
import service.UserService;

import java.time.LocalDateTime;

public class ConsultationMain {
    public static void main(String[] args) {
        // First, create a Title to associate with the User
        TitleService titleService = new TitleService();
        Title title = new Title("Consultation Title", 500, 200);
        titleService.createPst(title);

        // Fetch the created Title
        Title createdTitle = titleService.readAll().get(0);

        // Create a User to associate with the Profile and Consultation
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

        // Create a Profile for the User
        ProfileService profileService = new ProfileService();
        // Check if a Profile already exists for this user and delete it (due to unique constraint on user_id)
        Profile existingProfile = profileService.findByUserId(createdUser.getId());
        if (existingProfile != null) {
            System.out.println("Deleting existing profile for user_id " + createdUser.getId() + "...");
            profileService.delete(existingProfile);
        }
        Profile profile = new Profile(
                createdUser, // userId
                "Experienced professional with 10 years in the field.", // biographie
                "Cardiology", // specialite
                "Books, Videos, Articles", // ressources
                120.50, // prixConsultation
                36.806494, // latitude
                10.181532 // longitude
        );
        profileService.createPst(profile);

        // Fetch the created Profile
        Profile createdProfile = profileService.readAll().get(0);

        // Create a new Consultation object
        Consultation consultation = new Consultation(
                createdUser, // userId
                createdProfile, // profileId
                LocalDateTime.now(), // consultationDate (current date and time)
                false // isCompleted
        );

        // Instantiate the ConsultationService
        ConsultationService cs = new ConsultationService();

        // Test CRUD operations
        // 1. Create a new record
        System.out.println("Creating a new consultation...");
        cs.createPst(consultation);

        // 2. Read all consultations
        System.out.println("\nReading all consultations:");
        cs.readAll().forEach(System.out::println);

        // 3. Update the consultation (using the first record for demo)
        System.out.println("\nUpdating the first consultation...");
        Consultation consultationToUpdate = cs.readAll().get(0); // Get the first consultation
        consultationToUpdate.setConsultationDate(LocalDateTime.now().plusDays(1)); // Schedule for tomorrow
        consultationToUpdate.setCompleted(true); // Mark as completed
        cs.update(consultationToUpdate);

        // 4. Read all consultations again to see the update
        System.out.println("\nReading all consultations after update:");
        cs.readAll().forEach(System.out::println);

        // 5. Delete the consultation
        System.out.println("\nDeleting the consultation...");
        cs.delete(consultationToUpdate);

        // 6. Read all consultations to confirm deletion
        System.out.println("\nReading all consultations after deletion:");
        cs.readAll().forEach(System.out::println);

        // 7. Test readById (assuming ID 1 exists, adjust as needed)
        System.out.println("\nReading consultation with ID 1:");
        Consultation consultationById = cs.readById(1);
        System.out.println(consultationById != null ? consultationById : "Consultation with ID 1 not found.");
    }
}