package entite;

import java.util.ArrayList;
import java.util.List;

public class Cours {
    private int id;
    private int moduleId;
    private String title;
    private List<Quiz> quizzes; // Liste des quiz liés à ce cours

    // Constructeur par défaut
    public Cours() {
        this.quizzes = new ArrayList<>(); // Initialiser la liste pour éviter les erreurs null
    }

    // Constructeur avec tous les attributs
    public Cours(int id, int moduleId, String title) {
        this.id = id;
        this.moduleId = moduleId;
        this.title = title;
        this.quizzes = new ArrayList<>(); // Toujours initialiser la liste
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Quiz> getQuizzes() {
        return quizzes;
    }

    // Ajouter un quiz à la liste des quiz associés à ce cours
    public void addQuiz(Quiz quiz) {
        this.quizzes.add(quiz);
    }


    @Override
    public String toString() {
        return title; // Utile pour le ComboBox si nécessaire
    }
}
