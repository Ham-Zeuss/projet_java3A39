package test;

import entite.StoreItem;
import entite.Title;
import service.StoreItemService;
import service.TitleService;

public class StoreItemMain {
    public static void main(String[] args) {
        // First, create a Title to associate with the StoreItem
        TitleService titleService = new TitleService();
        Title title = new Title("Test Title", 100, 50);
        titleService.createPst(title);

        // Fetch the created Title (assuming it's the first one for simplicity)
        Title createdTitle = titleService.readAll().get(0);

        // Create a new StoreItem object
        StoreItem storeItem = new StoreItem(
                createdTitle,
                "Sample Item",
                "This is a sample item description.",
                99,
                "sample_image.jpg",
                10
        );

        // Instantiate the StoreItemService
        StoreItemService sis = new StoreItemService();

        // Test CRUD operations
        // 1. Create a new record
        System.out.println("Creating a new store item...");
        sis.createPst(storeItem);

        // 2. Read all store items
        System.out.println("\nReading all store items:");
        sis.readAll().forEach(System.out::println);

        // 3. Update the store item (using the first record for demo)
        System.out.println("\nUpdating the first store item...");
        StoreItem itemToUpdate = sis.readAll().get(0); // Get the first item
        itemToUpdate.setName("Updated Item");
        itemToUpdate.setDescription(null); // Test nullable field
        itemToUpdate.setPrice(150);
        itemToUpdate.setImage(null); // Test nullable field
        itemToUpdate.setStock(5);
        sis.update(itemToUpdate);

        // 4. Read all store items again to see the update
        System.out.println("\nReading all store items after update:");
        sis.readAll().forEach(System.out::println);

        // 5. Delete the store item
        System.out.println("\nDeleting the store item...");
        sis.delete(itemToUpdate);

        // 6. Read all store items to confirm deletion
        System.out.println("\nReading all store items after deletion:");
        sis.readAll().forEach(System.out::println);

        // 7. Test readById (assuming ID 1 exists, adjust as needed)
        System.out.println("\nReading store item with ID 1:");
        StoreItem itemById = sis.readById(1);
        System.out.println(itemById != null ? itemById : "Store item with ID 1 not found.");
    }
}