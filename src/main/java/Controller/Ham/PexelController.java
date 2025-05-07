package Controller.Ham;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entite.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import service.PexelWordService;
import service.UserService;
import entite.PexelWord;
import entite.User;
import test.Ham.WordScrambleGame;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PexelController {
    private static final String PEXELS_API_KEY = "M2NUFDmgYXzCAulwxY9W0MtYFE4oIjgmQeSPLxkPtA3EBd59yrShuOqr";
    private static final int CACHE_EXPIRE_MINUTES = 60;

    private final Cache<String, Image> imageCache = Caffeine.newBuilder()
            .expireAfterWrite(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .build();

    private final OkHttpClient client = new OkHttpClient();
    private final Random random = new Random();
    private final PexelWordService pexelWordService = new PexelWordService();
    private final UserService userService = new UserService();

    @FXML private FlowPane wordsContainer;
    @FXML private FlowPane imagesContainer;
    @FXML private Button easyModeButton;
    @FXML private Button hardModeButton;
    @FXML private Button restartButton;
    @FXML private VBox gameContainer;
    @FXML private Label timerLabel;
    @FXML private Label attemptsLabel;
    @FXML private Label scoreLabel;

    private List<String> currentWords = new ArrayList<>();
    private String selectedWord = null;
    private ImageView selectedImageView = null;
    private int correctMatches = 0;
    private int totalMatches = 0;

    private Button firstSelectedWord = null;
    private ImageView firstSelectedImage = null;
    private int attempts = 0;
    private Timer gameTimer = new Timer();
    private int secondsPlayed = 0;

    @FXML
    public void initialize() {
        setupButtons();
        startTimer();
    }

    private void startTimer() {
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsPlayed++;
                Platform.runLater(() -> {
                    if (timerLabel != null) {
                        timerLabel.setText("Time: " + secondsPlayed + "s");
                    }
                    updateScoreDisplay();
                });
            }
        }, 1000, 1000);
    }

    private void updateScoreDisplay() {
        attemptsLabel.setText("Attempts: " + attempts);
        scoreLabel.setText("Score: " + calculateScore());
    }

    private void setupButtons() {
        easyModeButton.setOnAction(e -> startGame("easy"));
        hardModeButton.setOnAction(e -> startGame("hard"));
        restartButton.setOnAction(e -> resetGame());
    }

    private void startGame(String difficulty) {
        resetGame();

        List<String> wordPool = getWordsByDifficulty(difficulty);
        if (wordPool.isEmpty()) {
            Platform.runLater(() -> showError("No words found for difficulty: " + difficulty));
            return;
        }

        Collections.shuffle(wordPool);

        totalMatches = Math.min(7, wordPool.size());
        currentWords = new ArrayList<>(wordPool.subList(0, totalMatches));

        loadWords();
        loadImages();
    }

    private List<String> getWordsByDifficulty(String difficulty) {
        List<PexelWord> pexelWords = pexelWordService.readByDifficulty(difficulty);
        List<String> words = new ArrayList<>();
        for (PexelWord pexelWord : pexelWords) {
            words.add(pexelWord.getWord());
        }
        return words;
    }

    private void loadWords() {
        wordsContainer.getChildren().clear();

        List<String> shuffledWords = new ArrayList<>(currentWords);
        Collections.shuffle(shuffledWords);

        for (String word : shuffledWords) {
            Button wordButton = new Button(word);
            wordButton.getStyleClass().setAll("word-button");
            wordButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
            wordButton.setPrefSize(150, 60);
            wordButton.setOnAction(e -> handleWordClick(word, wordButton));
            wordsContainer.getChildren().add(wordButton);
        }
    }

    private void loadImages() {
        imagesContainer.getChildren().clear();

        new Thread(() -> {
            for (String word : currentWords) {
                try {
                    Image image = getImageForWord(word);
                    Platform.runLater(() -> addImageToContainer(word, image));
                } catch (IOException e) {
                    Platform.runLater(() -> showError("Failed to load image for: " + word));
                }
            }
        }).start();
    }

    private Image getImageForWord(String word) throws IOException {
        Image cachedImage = imageCache.getIfPresent(word);
        if (cachedImage != null) {
            return cachedImage;
        }

        Request request = new Request.Builder()
                .url("https://api.pexels.com/v1/search?query=" + word + "&per_page=1")
                .header("Authorization", PEXELS_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            String imageUrl = json.getAsJsonArray("photos")
                    .get(0).getAsJsonObject()
                    .get("src").getAsJsonObject()
                    .get("medium").getAsString();

            Request imageRequest = new Request.Builder().url(imageUrl).build();
            try (Response imageResponse = client.newCall(imageRequest).execute()) {
                InputStream inputStream = imageResponse.body().byteStream();
                Image image = new Image(inputStream);
                imageCache.put(word, image);
                return image;
            }
        }
    }

    private void addImageToContainer(String word, Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("image-card");
        imageView.setUserData(word);
        imageView.setOnMouseClicked(e -> handleImageClick(word, imageView));
        imagesContainer.getChildren().add(imageView);
    }

    private void showCongratulations() {
        int score = calculateScore();
        Session session = Session.getInstance();

        if (session.isActive()) {
            int userId = session.getUserId();
            User user = userService.readById(userId);
            if (user != null) {
                Integer currentScoreTotal = user.getScoreTotal() != null ? user.getScoreTotal() : 0;
                int newScoreTotal = currentScoreTotal + score;
                userService.updateScoreTotal(userId, newScoreTotal);
                System.out.println("Updated score_total for user ID " + userId + ": +" + score + " -> " + newScoreTotal);
            } else {
                System.out.println("User with ID " + userId + " not found");
            }
        } else {
            System.out.println("No active session found");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText("🎉 You matched all pairs in " + secondsPlayed + " seconds! 🎉");
        alert.setContentText("Attempts: " + attempts + "\nScore: " + score);
        alert.showAndWait();
    }

    private void resetGame() {
        wordsContainer.getChildren().clear();
        imagesContainer.getChildren().clear();
        firstSelectedWord = null;
        firstSelectedImage = null;
        correctMatches = 0;
        totalMatches = 0;
        attempts = 0;
        secondsPlayed = 0;
        gameTimer.cancel();
        gameTimer = new Timer();
        startTimer();
        updateScoreDisplay();
        Platform.runLater(() -> {
            timerLabel.setText("Time: 0s");
            attemptsLabel.setText("Attempts: 0");
            scoreLabel.setText("Score: 0");
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleWordClick(String word, Button wordButton) {
        if (wordButton.getStyleClass().contains("correct") ||
                (firstSelectedWord != null && firstSelectedImage != null)) {
            return;
        }

        wordButton.getStyleClass().add("selected");

        if (firstSelectedWord == null) {
            firstSelectedWord = wordButton;
        } else {
            wordButton.getStyleClass().remove("selected");
        }

        checkForMatch();
    }

    private void handleImageClick(String word, ImageView imageView) {
        if (imageView.getStyleClass().contains("correct") ||
                (firstSelectedWord != null && firstSelectedImage != null)) {
            return;
        }

        imageView.getStyleClass().add("selected");

        if (firstSelectedImage == null) {
            firstSelectedImage = imageView;
        } else {
            imageView.getStyleClass().remove("selected");
        }

        checkForMatch();
    }

    private void checkForMatch() {
        if (firstSelectedWord != null && firstSelectedImage != null) {
            attempts++;
            boolean isMatch = firstSelectedWord.getText().equals(firstSelectedImage.getUserData().toString());

            if (isMatch) {
                firstSelectedWord.getStyleClass().remove("selected");
                firstSelectedWord.getStyleClass().add("correct");
                firstSelectedImage.getStyleClass().remove("selected");
                firstSelectedImage.getStyleClass().add("correct");
                correctMatches++;

                if (correctMatches == totalMatches) {
                    gameTimer.cancel();
                    showCongratulations();
                }
            } else {
                firstSelectedWord.getStyleClass().add("wrong-temporarily");
                firstSelectedImage.getStyleClass().add("wrong-temporarily");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            firstSelectedWord.getStyleClass().remove("selected");
                            firstSelectedWord.getStyleClass().remove("wrong-temporarily");
                            firstSelectedImage.getStyleClass().remove("selected");
                            firstSelectedImage.getStyleClass().remove("wrong-temporarily");
                        });
                    }
                }, 1000);
            }

            firstSelectedWord = null;
            firstSelectedImage = null;
            updateScoreDisplay();
        }
    }

    private int calculateScore() {
        return Math.max(0, 5000 - (secondsPlayed * 10) - (attempts * 20));
    }

    public static class GameController {
        private final WordScrambleGame game = new WordScrambleGame();
        private WordScrambleGame.WordData currentWordData;
        private int score = 0;
        private int attempts = 0;
        private final int maxAttempts = 3;
        private boolean isGameActive = true;
        private String[] revealedLetters;
        private final UserService userService = new UserService();
        private final Session session = Session.getInstance();

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

            // Update user's score_total
            if (session.isActive()) {
                int userId = session.getUserId();
                User user = userService.readById(userId);
                if (user != null) {
                    Integer currentScoreTotal = user.getScoreTotal() != null ? user.getScoreTotal() : 0;
                    int newScoreTotal = currentScoreTotal + score;
                    userService.updateScoreTotal(userId, newScoreTotal);
                    System.out.println("Updated score_total for user ID " + userId + ": +" + score + " -> " + newScoreTotal);
                    score = 0; // Reset score for the next word
                    scoreLabel.setText("Score: " + score);
                } else {
                    System.out.println("User with ID " + userId + " not found");
                }
            } else {
                System.out.println("No active session found");
            }
        }

        private void closeGame() {
            // Navigate back to the previous page (e.g., main menu or dashboard)
            Stage stage = (Stage) root.getScene().getWindow();
            navigateToMainMenu(stage);
        }

        private void navigateToMainMenu(Stage stage) {
            try {
                // Create a VBox to stack the header, header image, body, and footer
                VBox mainContent = new VBox();
                mainContent.setAlignment(Pos.TOP_CENTER);

                // 1. Load header.fxml
                FXMLLoader headerFxmlLoader = new FXMLLoader(getClass().getResource("/header.fxml"));
                VBox headerFxmlContent = headerFxmlLoader.load();
                headerFxmlContent.setPrefSize(1000, 100);
                mainContent.getChildren().add(headerFxmlContent);

                // 2. Add header image
                ImageView headerImageView = new ImageView();
                try {
                    Image headerImage = new Image(getClass().getResourceAsStream("/header.png"));
                    headerImageView.setImage(headerImage);
                    headerImageView.setPreserveRatio(true);
                    headerImageView.setFitWidth(1920);
                    headerImageView.setSmooth(true);
                    headerImageView.setCache(true);
                    VBox.setMargin(headerImageView, new Insets(0, 0, 10, 0));
                } catch (Exception e) {
                    System.err.println("Error loading header image: " + e.getMessage());
                    Rectangle fallbackHeader = new Rectangle(1000, 150, Color.LIGHTGRAY);
                    Label errorLabel = new Label("Header image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackHeader);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(headerImageView);

                // 3. Load body content
                URL resourceUrl = getClass().getResource("/HamzaFXML/GamesMenu.fxml");
                if (resourceUrl == null) {
                    System.err.println("Resource not found: /HamzaFXML/GamesMenu.fxml");
                    return;
                }
                FXMLLoader bodyLoader = new FXMLLoader(resourceUrl);
                Parent bodyContent = bodyLoader.load();
                bodyContent.setStyle("-fx-pref-width: 1920; -fx-pref-height: 1000; -fx-max-height: 2000;");
                mainContent.getChildren().add(bodyContent);

                // 4. Load footer as ImageView
                ImageView footerImageView = new ImageView();
                try {
                    Image footerImage = new Image(getClass().getResourceAsStream("/footer.png"));
                    footerImageView.setImage(footerImage);
                    footerImageView.setPreserveRatio(true);
                    footerImageView.setFitWidth(1920);
                } catch (Exception e) {
                    System.err.println(" Tolkien loading footer image: " + e.getMessage());
                    Rectangle fallbackFooter = new Rectangle(1000, 100, Color.LIGHTGRAY);
                    Label errorLabel = new Label("Footer image not found");
                    errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red;");
                    VBox fallbackBox = new VBox(errorLabel, fallbackFooter);
                    mainContent.getChildren().add(fallbackBox);
                }
                mainContent.getChildren().add(footerImageView);

                // Wrap the VBox in a ScrollPane
                ScrollPane scrollPane = new ScrollPane(mainContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                // Create a Scene
                Scene scene = new Scene(scrollPane, 1920, 1080);

                // Add CSS files
                URL storeCards = getClass().getResource("/css/store-cards.css");
                if (storeCards != null) {
                    scene.getStylesheets().add(storeCards.toExternalForm());
                }

                URL navBar = getClass().getResource("/navbar.css");
                if (navBar != null) {
                    scene.getStylesheets().add(navBar.toExternalForm());
                }

                URL gooButton = getClass().getResource("/css/GooButton.css");
                if (gooButton != null) {
                    scene.getStylesheets().add(gooButton.toExternalForm());
                } else {
                    System.err.println("CSS file not found: /css/GooButton.css");
                }

                // Set the scene and show the stage
                stage.setScene(scene);
                stage.setTitle("Games Menu");
                stage.show();
            } catch (IOException e) {
                System.err.println("Error loading resources for path: HamzaFXML/GamesMenu.fxml");
                e.printStackTrace();
            }
        }
    }
}