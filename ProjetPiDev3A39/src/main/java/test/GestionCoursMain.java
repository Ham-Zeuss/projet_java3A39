package test;

import entite.Cours;
import entite.Module;
import service.CoursService;
import service.ModuleService;

import java.time.LocalDateTime;

public class GestionCoursMain {
    public static void main(String[] args) {
        // Instantiate services
        ModuleService moduleService = new ModuleService();
        CoursService coursService = new CoursService();

        // Step 1: Create a new Module
        System.out.println("Creating a new module...");
        Module module = new Module("Physics", "Intro to Physics", 5, "Intermediate");
        moduleService.createPst(module);

        // Fetch the created module (assuming it's the last one in the list)
        Module createdModule = moduleService.readAll().get(moduleService.readAll().size() - 1);
        System.out.println("Created Module: " + createdModule);

        // Step 2: Add a new Cours under the created Module
        System.out.println("\nAdding a new course under the module...");
        Cours cours = new Cours("Quantum Mechanics", createdModule, "quantum_mechanics.pdf");
        coursService.createPst(cours);

        // Fetch the created course (assuming it's the last one in the list)
        Cours createdCours = coursService.readAll().get(coursService.readAll().size() - 1);
        System.out.println("Created Course: " + createdCours);

        // Step 3: Update the Course
        System.out.println("\nUpdating the course...");
        createdCours.setTitle("Updated Quantum Mechanics");
        createdCours.setUpdatedAt(LocalDateTime.now());
        coursService.update(createdCours);

        // Verify the update
        Cours updatedCours = coursService.readById(createdCours.getId());
        System.out.println("Updated Course: " + updatedCours);

        // Step 4: Update the Module
        System.out.println("\nUpdating the module...");
        createdModule.setTitle("Updated Physics");
        moduleService.update(createdModule);

        // Verify the update
        Module updatedModule = moduleService.readById(createdModule.getId());
        System.out.println("Updated Module: " + updatedModule);

        // Step 5: Attempt to delete the Module
        System.out.println("\nAttempting to delete the module...");
        try {
            moduleService.delete(updatedModule);
            System.out.println("Module deleted successfully.");
        } catch (Exception e) {
            System.err.println("Failed to delete module: " + e.getMessage());
        }

        // Step 6: Read all modules and courses to confirm deletion
        System.out.println("\nReading all modules after deletion:");
        moduleService.readAll().forEach(System.out::println);

        System.out.println("\nReading all courses after deletion:");
        coursService.readAll().forEach(System.out::println);
    }
}