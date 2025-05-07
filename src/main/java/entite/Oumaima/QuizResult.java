package entite.Oumaima;

import entite.User;

public class QuizResult {
    private int id;
    private Quiz quiz; // Référence à l'entité Quiz
    private User user; // Référence à l'entité User
    private float note;

    // Constructeur par défaut
    public QuizResult() {
    }

    // Constructeur avec tous les champs
    public QuizResult(int id, Quiz quiz, User user, float note) {
        this.id = id;
        this.quiz = quiz;
        this.user = user;
        this.note = note;
    }

    // Constructeur sans id (pour les nouvelles insertions)
    public QuizResult(Quiz quiz, User user, float note) {
        this.quiz = quiz;
        this.user = user;
        this.note = note;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public float getNote() {
        return note;
    }

    public void setNote(float note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "QuizResult{" +
                "id=" + id +
                ", quiz=" + (quiz != null ? quiz.getTitle() : "null") +
                ", user=" + (user != null ? user.getFullName() : "null") +
                ", note=" + note +
                '}';
    }
}