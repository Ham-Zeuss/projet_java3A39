package Controller.Boubaker;

import entite.Session;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import okhttp3.*;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller for KPI Chatbot, managing messages, API calls, and UI interactivity.
 */
public class ChatbotController {
    private static final Logger LOGGER = Logger.getLogger(ChatbotController.class.getName());
    private static final String CONFIG_FILE = "/chatbot.properties";
    private static final String PROMPTS_FILE = "/prompts.json";
    private static final String CHAT_HISTORY_DIR = "chat_histories";
    private static final int MAX_INPUT_LENGTH = 500;
    private final OkHttpClient client = new OkHttpClient();
    private String ollamaUrl;
    private String ollamaModel;
    private String basePrompt;
    private String parentPrompt;
    private String adminPrompt;
    private int userId;

    @FXML private TextField chatbotInput;
    @FXML private Button sendButton;
    @FXML private Button clearButton;
    @FXML private VBox chatContainer; // Contains user and bot messages
    @FXML private ScrollPane chatScroll; // ScrollPane for chat messages
    @FXML private Text chatbotTitle;
    @FXML private Text errorLabel;
    @FXML private Text charCounter;

    /**
     * Initializes the UI with history, welcome message, and interactivity.
     */
    @FXML
    private void initialize() {
        loadConfig();
        loadPrompts();
        setupUser();

        chatbotTitle.setText("KPI Chatbot");

        // Load chat history
        loadChatHistory();

        // Add welcome message
        addBotMessage("Bot: Welcome to KPI Packs! Ask me anything!");

        // Auto-scroll to bottom
        chatContainer.heightProperty().addListener((obs, old, newVal) ->
                Platform.runLater(() -> chatScroll.setVvalue(1.0)));

        // Enable Enter key
        chatbotInput.setOnAction(event -> handleSend());

        // Update character counter
        chatbotInput.textProperty().addListener((obs, old, newVal) -> {
            errorLabel.setVisible(false);
            errorLabel.setText("");
            int length = newVal != null ? newVal.length() : 0;
            charCounter.setText(length + "/" + MAX_INPUT_LENGTH);
        });
    }

    /**
     * Loads API configuration.
     */
    private void loadConfig() {
        Properties props = new Properties();
        try (var stream = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (stream != null) {
                props.load(stream);
                ollamaUrl = props.getProperty("ollama.url", "http://localhost:11434/api/generate");
                ollamaModel = props.getProperty("ollama.model", "deepseek-r1:1.5b");
            } else {
                LOGGER.warning("Config file not found, using defaults");
                ollamaUrl = "http://localhost:11434/api/generate";
                ollamaModel = "deepseek-r1:1.5b";
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load config", e);
            ollamaUrl = "http://localhost:11434/api/generate";
            ollamaModel = "deepseek-r1:1.5b";
        }
        LOGGER.info("Ollama URL: " + ollamaUrl + ", Model: " + ollamaModel);
    }

    /**
     * Loads prompts from prompts.json.
     */
    private void loadPrompts() {
        try (var stream = getClass().getResourceAsStream(PROMPTS_FILE)) {
            if (stream == null) {
                LOGGER.severe("Prompts file not found");
                basePrompt = "You are a helpful chatbot.";
                parentPrompt = "";
                adminPrompt = "";
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonArray prompts = json.getAsJsonArray("prompts");

            for (int i = 0; i < prompts.size(); i++) {
                JsonObject prompt = prompts.get(i).getAsJsonObject();
                String id = prompt.get("id").getAsString();
                String text = prompt.get("text").getAsString();
                switch (id) {
                    case "base":
                        basePrompt = text;
                        break;
                    case "parent":
                        parentPrompt = text;
                        break;
                    case "admin":
                        adminPrompt = text;
                        break;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load prompts", e);
            basePrompt = "You are a helpful chatbot.";
            parentPrompt = "";
            adminPrompt = "";
        }
    }

    /**
     * Sets up the current user.
     */
    private void setupUser() {
        Session session = Session.getInstance();
        userId = session.getUserId();
        if (userId <= 0 || !session.isActive()) {
            showError("User not logged in. Please log in to use the chatbot.");
            setInputEnabled(false);
        }
    }

    /**
     * Loads chat history for the current user.
     */
    private void loadChatHistory() {
        File dir = new File(CHAT_HISTORY_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String fileName = String.format("%s/user_%d.txt", CHAT_HISTORY_DIR, userId);
        File file = new File(fileName);
        if (!file.exists()) {
            LOGGER.info("No chat history found for user " + userId);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("User: ")) {
                    addUserMessage(line);
                } else if (line.startsWith("Bot: ")) {
                    addBotMessage(line);
                } else if (line.startsWith("Achat - ")) {
                    addBotMessage("Bot: Historique d'achat - " + line);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load chat history for user " + userId, e);
        }
    }

    /**
     * Saves message to user-specific history.
     */
    private void saveChatHistory(String message) {
        File dir = new File(CHAT_HISTORY_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String fileName = String.format("%s/user_%d.txt", CHAT_HISTORY_DIR, userId);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save chat history for user " + userId, e);
        }
    }

    /**
     * Handles send action.
     */
    @FXML
    private void handleSend() {
        String input = chatbotInput.getText().trim();
        if (input.isEmpty()) {
            showError("Please enter a message.");
            return;
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            showError("Message too long! Keep it under " + MAX_INPUT_LENGTH + " characters.");
            return;
        }

        String userMessage = "User: " + input;
        addUserMessage(userMessage);
        saveChatHistory(userMessage);
        chatbotInput.clear();
        setInputEnabled(false);

        Task<String> apiTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return queryOllama(input);
            }
        };

        apiTask.setOnSucceeded(event -> {
            String response = apiTask.getValue();
            String botMessage = "Bot: " + response;
            addBotMessage(botMessage);
            saveChatHistory(botMessage);
            setInputEnabled(true);
        });

        apiTask.setOnFailed(event -> {
            Throwable e = apiTask.getException();
            LOGGER.log(Level.SEVERE, "API call failed", e);
            String errorMsg = switch (e) {
                case IOException io -> "Bot: Network error, please check your connection.";
                case IllegalStateException ise -> "Bot: API response invalid.";
                default -> "Bot: Oops, something went wrong! Try again.";
            };
            addBotMessage(errorMsg);
            setInputEnabled(true);
        });

        new Thread(apiTask).start();
    }

    /**
     * Clears chat UI and history.
     */
    @FXML
    private void clearChat() {
        chatContainer.getChildren().clear();
        String fileName = String.format("%s/user_%d.txt", CHAT_HISTORY_DIR, userId);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to clear chat history for user " + userId, e);
        }
        addBotMessage("Bot: Chat cleared. Ask me anything!");
    }

    /**
     * Adds bot message (left).
     */
    private void addBotMessage(String message) {
        Text label = new Text(message);
        label.getStyleClass().add("bot-message");
        label.wrappingWidthProperty().bind(chatScroll.widthProperty().subtract(20));
        HBox hbox = new HBox(label);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hbox.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        // Add sliding animation
        TranslateTransition slide = new TranslateTransition(javafx.util.Duration.millis(300), hbox);
        slide.setFromX(-20);
        slide.setToX(0);
        slide.play();

        Platform.runLater(() -> {
            chatContainer.getChildren().add(hbox);
            chatScroll.setVvalue(1.0); // Auto-scroll to bottom
        });
    }

    /**
     * Adds user message (right).
     */
    private void addUserMessage(String message) {
        Text label = new Text(message);
        label.getStyleClass().add("user-message");
        label.wrappingWidthProperty().bind(chatScroll.widthProperty().subtract(20));
        HBox hbox = new HBox(label);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        hbox.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        // Add sliding animation
        TranslateTransition slide = new TranslateTransition(javafx.util.Duration.millis(300), hbox);
        slide.setFromX(20);
        slide.setToX(0);
        slide.play();

        Platform.runLater(() -> chatContainer.getChildren().add(hbox));
    }

    /**
     * Shows error message.
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        });
    }

    /**
     * Enables/disables input.
     */
    private void setInputEnabled(boolean enabled) {
        Platform.runLater(() -> {
            chatbotInput.setDisable(!enabled);
            sendButton.setDisable(!enabled);
            clearButton.setDisable(!enabled);
            if (enabled) {
                chatbotInput.requestFocus();
            }
        });
    }

    /**
     * Queries Ollama API with prompts included.
     */
    private String queryOllama(String input) throws IOException {
        // Combine prompts with user input
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(basePrompt).append("\n");
        fullPrompt.append(parentPrompt).append("\n");
        fullPrompt.append(adminPrompt).append("\n");
        fullPrompt.append("User input: ").append(input);

        JSONObject payload = new JSONObject()
                .put("model", ollamaModel)
                .put("prompt", fullPrompt.toString())
                .put("stream", true);

        Request request = new Request.Builder()
                .url(ollamaUrl)
                .post(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                .build();

        StringBuilder responseBuilder = new StringBuilder();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama request failed: " + response.code());
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            String[] lines = responseBody.string().split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                JSONObject json = new JSONObject(line);
                String text = json.getString("response");
                if (!text.contains("<think>") && !text.contains("</think>")) {
                    responseBuilder.append(text);
                }
                if (json.getBoolean("done")) break;
            }
        }
        String result = responseBuilder.toString().trim();
        LOGGER.info("Ollama response: " + result);
        return result.isEmpty() ? "I'm thinking... Ask something else!" : result;
    }

    /**
     * Cleans up resources.
     */
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        LOGGER.info("ChatbotController shutdown complete");
    }
}