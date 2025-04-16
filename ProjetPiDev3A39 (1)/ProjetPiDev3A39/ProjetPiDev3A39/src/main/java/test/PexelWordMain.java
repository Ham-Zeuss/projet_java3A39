package test;

import entite.PexelWord;
import service.PexelWordService;

public class PexelWordMain {
    public static void main(String[] args) {
        // Create a new PexelWord object
        PexelWord pexelWord = new PexelWord("plane", "easy");

        // Instantiate the PexelWordService
        PexelWordService pws = new PexelWordService();

        // Test CRUD operations
        // 1. Create a new record
        System.out.println("Creating a new pexel word...");
        pws.createPst(pexelWord);

        // 2. Read all pexel words
        System.out.println("\nReading all pexel words:");
        pws.readAll().forEach(System.out::println);

        // 3. Update the pexel word (using the first record for demo)
        System.out.println("\nUpdating the first pexel word...");
        PexelWord wordToUpdate = pws.readAll().get(0); // Get the first word
        wordToUpdate.setWord("updated");
        wordToUpdate.setDifficulty("hard");
        pws.update(wordToUpdate);

        // 4. Read all pexel words again to see the update
        System.out.println("\nReading all pexel words after update:");
        pws.readAll().forEach(System.out::println);

        // 5. Delete the pexel word
        System.out.println("\nDeleting the pexel word...");
        pws.delete(wordToUpdate);

        // 6. Read all pexel words to confirm deletion
        System.out.println("\nReading all pexel words after deletion:");
        pws.readAll().forEach(System.out::println);

        // 7. Test readById (assuming ID 1 exists, adjust as needed)
        System.out.println("\nReading pexel word with ID 1:");
        PexelWord wordById = pws.readById(132);
        System.out.println(wordById != null ? wordById : "Pexel word with ID 1 not found.");
    }
}