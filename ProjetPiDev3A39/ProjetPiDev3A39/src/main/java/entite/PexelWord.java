package entite;

public class PexelWord {
    private int id;
    private String word;
    private String difficulty;

    // Default constructor
    public PexelWord() {
    }

    // Constructor with all fields (including id)
    public PexelWord(int id, String word, String difficulty) {
        this.id = id;
        this.word = word;
        this.difficulty = difficulty;
    }

    // Constructor without id (useful for creating new records)
    public PexelWord(String word, String difficulty) {
        this.word = word;
        this.difficulty = difficulty;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "PexelWord{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}