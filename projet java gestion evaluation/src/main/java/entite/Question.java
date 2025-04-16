package entite;

public class Question {
    private int id;
    private Quiz quiz; // Association avec la classe Quiz
    private String text;
    private String optionType;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String correctAnswers; // Stocké comme JSON (ex: "[1, 3]" pour une QCM)
    private String reponseSoumise; // Stocké comme JSON (ex: "[2]" pour la réponse soumise)

    // Constructeur par défaut
    public Question() {
    }

    // Constructeur avec tous les attributs
    public Question(int id, Quiz quiz, String text, String type, String option1, String option2,
                    String option3, String option4, String correctAnswers, String reponseSoumise) {
        this.id = id;
        this.quiz = quiz;
        this.text = text;
        this.optionType = type;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswers = correctAnswers;
        this.reponseSoumise = reponseSoumise;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public String getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(String correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public String getReponseSoumise() {
        return reponseSoumise;
    }

    public void setReponseSoumise(String reponseSoumise) {
        this.reponseSoumise = reponseSoumise;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", quiz=" + (quiz != null ? quiz.getTitle() : "null") + // Affichage du titre du quiz
                ", text='" + text + '\'' +
                ", type='" + optionType + '\'' +
                ", option1='" + option1 + '\'' +
                ", option2='" + option2 + '\'' +
                ", option3='" + option3 + '\'' +
                ", option4='" + option4 + '\'' +
                ", correctAnswers='" + correctAnswers + '\'' +
                ", reponseSoumise='" + reponseSoumise + '\'' +
                '}';
    }
}