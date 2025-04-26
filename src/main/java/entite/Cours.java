package entite;

import java.time.LocalDateTime;

public class Cours {

    private Integer id;
    private String title;
    private Module moduleId; // Reference to Module
    private String pdfName;
    private LocalDateTime updatedAt;

    public Cours() {}

    public Cours(String title, Module moduleId, String pdfName) {
        this.title = title;
        this.moduleId = moduleId;
        this.pdfName = pdfName;
        this.updatedAt = LocalDateTime.now();
    }

    public Cours(Integer id, String title, Module moduleId, String pdfName, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.moduleId = moduleId;
        this.pdfName = pdfName;
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

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", moduleId=" + moduleId +
                ", pdfName='" + pdfName + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}