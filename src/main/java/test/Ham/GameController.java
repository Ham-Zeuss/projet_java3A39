package test.Ham;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameController {
    private final WordScrambleGame game = new WordScrambleGame();
    private WordScrambleGame.WordData currentWordData;
    private int score = 0;
    private int attempts = 0;
    private final int maxAttempts = 3;
    private boolean isGameActive = true;
    private String[] revealedLetters;

    private final VBox root = new VBox(10);
    private final Label titleLabel = new Label("Word Scramble Game");
    private final Label scrambledWordLabel = new Label();
    private final Label meaningLabel = new Label();
    private final Label hintLabel = new Label();
    private final TextField guessField = new TextField();
    private final Button submitButton = new Button("Submit");
    private final Label resultLabel = new Label();
    private final Label scoreLabel = new Label("Score: 0");
    private final Button nextWordButton = new Button("Next Word");
    private final Button closeButton = new Button("Close");

    public GameController() {
        initializeUI();
        loadNewWord();
    }

    private void initializeUI() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        titleLabel.setFont(new Font(20));
        scrambledWordLabel.setFont(new Font(16));
        meaningLabel.setFont(new Font(14));
        hintLabel.setFont(new Font(14));
        resultLabel.setFont(new Font(14));
        scoreLabel.setFont(new Font(14));
        guessField.setPromptText("Enter your guess");
        guessField.setMaxWidth(200);
        nextWordButton.setVisible(false);

        submitButton.setOnAction(event -> checkGuess());
        nextWordButton.setOnAction(event -> loadNewWord());
        closeButton.setOnAction(event -> closeGame());

        root.getChildren().addAll(
                titleLabel,
                new Label("Unscramble this word:"),
                scrambledWordLabel,
                meaningLabel,
                hintLabel,
                guessField,
                submitButton,
                resultLabel,
                scoreLabel,
                nextWordButton,
                closeButton
        );
    }

    public VBox getView() {
        return root;
    }

    private void loadNewWord() {
        currentWordData = game.getNewWord();
        scrambledWordLabel.setText(currentWordData.scrambledWord);
        meaningLabel.setText("Meaning: " + currentWordData.meaning);
        hintLabel.setText("");
        guessField.setText("");
        guessField.setDisable(false);
        submitButton.setDisable(false);
        resultLabel.setText("");
        nextWordButton.setVisible(false);
        attempts = 0;
        isGameActive = true;
        revealedLetters = new String[currentWordData.originalWord.length()];
        System.out.println("Loaded new word: " + currentWordData.originalWord);
    }

    private void checkGuess() {
        if (!isGameActive) return;

        String guess = guessField.getText().trim().toLowerCase();
        System.out.println("User guessed: " + guess);
        attempts++;

        if (guess.equals(currentWordData.originalWord.toLowerCase())) {
            score++;
            resultLabel.setText("🎉 Correct! Well done!");
            scoreLabel.setText("Score: " + score);
            enableNextWord();
            System.out.println("Correct guess! Score: " + score);
        } else if (attempts >= maxAttempts) {
            resultLabel.setText("😢 Out of attempts! The word was: " + currentWordData.originalWord);
            enableNextWord();
            System.out.println("Out of attempts. Correct word: " + currentWordData.originalWord);
        } else {
            resultLabel.setText("😢 Incorrect. Try again!");
            provideHint();
            System.out.println("Incorrect guess. Attempts: " + attempts);
        }

        if (attempts >= maxAttempts) {
            guessField.setDisable(true);
            submitButton.setDisable(true);
        }
    }

    private void provideHint() {
        for (int i = 0; i < attempts && i < currentWordData.originalWord.length(); i++) {
            revealedLetters[i] = String.valueOf(currentWordData.originalWord.charAt(i));
        }
        StringBuilder hint = new StringBuilder();
        for (int i = 0; i < currentWordData.originalWord.length(); i++) {
            hint.append(revealedLetters[i] != null ? revealedLetters[i] : "_").append(" ");
        }
        hintLabel.setText("Hint: " + hint.toString().trim());
        System.out.println("Hint provided: " + hint.toString().trim());
    }

    private void enableNextWord() {
        nextWordButton.setVisible(true);
        isGameActive = false;
        guessField.setDisable(true);
        submitButton.setDisable(true);
    }

    private void closeGame() {
        // Save score to database for user ID 14
        updateScoreTotal(14, score);
        // Exit the application
        Platform.exit();
    }

    private void updateScoreTotal(int userId, int scoreToAdd) {
        String selectSql = "SELECT score_total FROM user WHERE id = ?";
        String updateSql = "UPDATE user SET score_total = ? WHERE id = ?";

        try (Connection conn = DataSource.getInstance().getConnection()) {
            // Retrieve current score_total
            Integer currentScoreTotal = null;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, userId);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    currentScoreTotal = rs.getObject("score_total") != null ? rs.getInt("score_total") : 0;
                } else {
                    System.out.println("User with ID " + userId + " not found");
                    return;
                }
            }

            // Update score_total
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                int newScoreTotal = currentScoreTotal + scoreToAdd;
                updateStmt.setInt(1, newScoreTotal);
                updateStmt.setInt(2, userId);
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Updated score_total for user ID " + userId + ": +" + scoreToAdd + " -> " + newScoreTotal);
                } else {
                    System.out.println("Failed to update score_total for user ID " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}