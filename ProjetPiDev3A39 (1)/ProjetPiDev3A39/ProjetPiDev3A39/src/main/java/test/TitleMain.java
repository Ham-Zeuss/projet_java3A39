package test;

import entite.Title;
import service.TitleService;

public class TitleMain {
    public static void main(String[] args) {
        // Create a new Title object
        Title title = new Title("Sample Title", 100, 50); // name, points_required, price (price can be null)

        // Instantiate the TitleService
        TitleService ts = new TitleService();

        // Test CRUD operations
        // 1. Create a new record
        System.out.println("Creating a new title...");
        ts.createPst(title);

        // 2. Read all titles
        System.out.println("\nReading all titles:");
        ts.readAll().forEach(System.out::println);

        // 3. Update the title (assuming the last inserted ID is needed, for demo we'll read the first title)
        System.out.println("\nUpdating the first title...");
        Title titleToUpdate = ts.readAll().get(0); // Get the first title (for demo purposes)
        titleToUpdate.setName("Updated Title");
        titleToUpdate.setpoints_required(200);
        titleToUpdate.setPrice(null); // Set price to null to test nullable field
        ts.update(titleToUpdate);

        // 4. Read all titles again to see the update
        System.out.println("\nReading all titles after update:");
        ts.readAll().forEach(System.out::println);

        // 5. Delete the title
        System.out.println("\nDeleting the title...");
        ts.delete(titleToUpdate);

        // 6. Read all titles to confirm deletion
        System.out.println("\nReading all titles after deletion:");
        ts.readAll().forEach(System.out::println);

        // 7. Test readById (assuming ID 1 exists, adjust as needed)
        System.out.println("\nReading title with ID 1:");
        Title titleById = ts.readById(1);
        System.out.println(titleById != null ? titleById : "Title with ID 1 not found.");
    }
}