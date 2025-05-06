package Controller.Ham;

import entite.Session;
import entite.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import service.UserService;
import test.Ham.WordScrambleGame;
import util.DataSource;

import java.io.IOException;
import java.net.URL;
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
    private final UserService userService = new UserService();
    private final Session session = Session.getInstance();

    private final VBox root = new VBox(10.0);
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
        this.initializeUI();
        this.loadNewWord();
    }

    private void initializeUI() {
        this.root.setAlignment(Pos.CENTER);
        this.root.setPadding(new Insets(20.0));
        this.titleLabel.setFont(new Font(20.0));
        this.scrambledWordLabel.setFont(new Font(16.0));
        this.meaningLabel.setFont(new Font(14.0));
        this.hintLabel.setFont(new Font(14.0));
        this.resultLabel.setFont(new Font(14.0));
        this.scoreLabel.setFont(new Font(14.0));
        this.guessField.setPromptText("Enter your guess");
        this.guessField.setMaxWidth(200.0);
        this.nextWordButton.setVisible(false);
        this.submitButton.setOnAction(event -> this.checkGuess());
        this.nextWordButton.setOnAction(event -> this.loadNewWord());
        this.closeButton.setOnAction(event -> this.closeGame());
        this.root.getChildren().addAll(
                this.titleLabel,
                new Label("Unscramble this word:"),
                this.scrambledWordLabel,
                this.meaningLabel,
                this.hintLabel,
                this.guessField,
                this.submitButton,
                this.resultLabel,
                this.scoreLabel,
                this.nextWordButton,
                this.closeButton
        );
    }

    public VBox getView() {
        return this.root;
    }

    private void loadNewWord() {
        this.currentWordData = this.game.getNewWord();
        this.scrambledWordLabel.setText(this.currentWordData.scrambledWord);
        this.meaningLabel.setText("Meaning: " + this.currentWordData.meaning);
        this.hintLabel.setText("");
        this.guessField.setText("");
        this.guessField.setDisable(false);
        this.submitButton.setDisable(false);
        this.resultLabel.setText("");
        this.nextWordButton.setVisible(false);
        this.attempts = 0;
        this.isGameActive = true;
        this.revealedLetters = new String[this.currentWordData.originalWord.length()];
        System.out.println("Loaded new word: " + this.currentWordData.originalWord);
    }

    private void checkGuess() {
        if (!this.isGameActive) {
            return;
        }

        String guess = this.guessField.getText().trim().toLowerCase();
        System.out.println("User guessed: " + guess);
        this.attempts++;

        if (guess.equals(this.currentWordData.originalWord.toLowerCase())) {
            this.score++;
            this.resultLabel.setText("🎉 Correct! Well done!");
            this.scoreLabel.setText("Score: " + this.score);
            this.enableNextWord();
            System.out.println("Correct guess! Score: " + this.score);
        } else if (this.attempts >= maxAttempts) {
            this.resultLabel.setText("😢 Out of attempts! The word was: " + this.currentWordData.originalWord);
            this.enableNextWord();
            System.out.println("Out of attempts. Correct word: " + this.currentWordData.originalWord);
        } else {
            this.resultLabel.setText("😢 Incorrect. Try again!");
            this.provideHint();
            System.out.println("Incorrect guess. Attempts: " + this.attempts);
        }

        if (this.attempts >= maxAttempts) {
            this.guessField.setDisable(true);
            this.submitButton.setDisable(true);
        }
    }

    private void provideHint() {
        for (int i = 0; i < this.attempts && i < this.currentWordData.originalWord.length(); i++) {
            this.revealedLetters[i] = String.valueOf(this.currentWordData.originalWord.charAt(i));
        }

        StringBuilder hint = new StringBuilder();
        for (int i = 0; i < this.currentWordData.originalWord.length(); i++) {
            hint.append(this.revealedLetters[i] != null ? this.revealedLetters[i] : "_").append(" ");
        }

        this.hintLabel.setText("Hint: " + hint.toString().trim());
        System.out.println("Hint provided: " + hint.toString().trim());
    }

    private void enableNextWord() {
        this.nextWordButton.setVisible(true);
        this.isGameActive = false;
        this.guessField.setDisable(true);
        this.submitButton.setDisable(true);

        if (session.isActive()) {
            int userId = session.getUserId();
            User user = userService.readById(userId);
            if (user != null) {
                Integer currentScoreTotal = user.getScoreTotal() != null ? user.getScoreTotal() : 0;
                int newScoreTotal = currentScoreTotal + this.score;
                userService.updateScoreTotal(userId, newScoreTotal);
                System.out.println("Updated score_total for user ID " + userId + ": +" + this.score + " -> " + newScoreTotal);
                this.score = 0;
                this.scoreLabel.setText("Score: " + this.score);
            } else {
                System.out.println("User with ID " + userId + " not found");
            }
        } else {
            System.out.println("No active session found");
        }
    }

    private void closeGame() {
        System.out.println("we're in GameController.java");
    }
}