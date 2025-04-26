package entite.Oumaima;

import java.util.Date;

public class Quiz {
    private int id;
    private Cours course; // Association avec la classe Cours
    private String title;
    private String description;
    private int duration;
    private Date createdAt;
    private float note;

    // Constructeur par défaut
    public Quiz() {
    }

    // Constructeur avec tous les attributs
    public Quiz(int id, Cours course, String title, String description, int duration, Date createdAt, float note) {
        this.id = id;
        this.course = course;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.createdAt = createdAt;
        this.note = note;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cours getCourse() {
        return course;
    }

    public void setCourse(Cours course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public float getNote() {
        return note;
    }

    public void setNote(float note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", course=" + (course != null ? course.getTitle() : "null") +  // Affichage du titre du cours
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", createdAt=" + createdAt +
                ", note=" + note +
                '}';
    }
    public Quiz(int id) {
        this.id = id;
        this.course = null;  // Vous pouvez laisser 'null' si vous ne l'avez pas encore défini
        this.title = "";     // Valeur par défaut
        this.description = ""; // Valeur par défaut
        this.duration = 0;   // Valeur par défaut
        this.createdAt = new Date(); // Date actuelle par défaut
        this.note = 0.0f;    // Valeur par défaut
    }

}
