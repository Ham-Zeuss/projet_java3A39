package entite;

import java.time.LocalDateTime;

public class Cours {

    private Integer id;
    private String title;
    private Module moduleId; // Reference to Module
    private String pdfName;
    private LocalDateTime updatedAt;
    private Integer userId; // Foreign key referencing the User table

    public Cours() {}

    // Constructor without ID (used for creating a new course)
    public Cours(String title, Module moduleId, String pdfName, Integer userId) {
        this.title = title;
        this.moduleId = moduleId;
        this.pdfName = pdfName;
        this.userId = userId; // Store the user's ID
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with ID (used for fetching an existing course from the database)
    public Cours(Integer id, String title, Module moduleId, String pdfName, Integer userId, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.moduleId = moduleId;
        this.pdfName = pdfName;
        this.userId = userId; // Store the user's ID
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Module getModuleId() {
        return moduleId;
    }

    public void setModuleId(Module moduleId) {
        this.moduleId = moduleId;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", moduleId=" + moduleId +
                ", pdfName='" + pdfName + '\'' +
                ", userId=" + userId + // Include userId in the string representation
                ", updatedAt=" + updatedAt +
                '}';
    }
}