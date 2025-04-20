package Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import okhttp3.*;
import org.json.JSONObject;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller for KPI Chatbot, managing messages, API calls, and UI interactivity.
 * Aligns user messages right, bot messages left, with no bottom empty space.
 */
public class ChatbotController {
    private static final Logger LOGGER = Logger.getLogger(ChatbotController.class.getName());
    private static final String CONFIG_FILE = "/chatbot.properties";
    private static final String HISTORY_FILE = "chat_history.txt";
    private static final int MAX_INPUT_LENGTH = 500;
    private final OkHttpClient client = new OkHttpClient();
    private String ollamaUrl;
    private String ollamaModel;

    @FXML private TextField chatbotInput;
    @FXML private Button sendButton;
    @FXML private Button clearButton;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScroll;
    @FXML private Text chatbotTitle;
    @FXML private Text errorLabel;
    @FXML private Text charCounter;

    /**
     * Initializes the UI with history, welcome message, and interactivity.
     */
    @FXML
    private void initialize() {
        loadConfig();
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
     * Loads chat history.
     */
    private void loadChatHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("User: ")) {
                    addUserMessage(line);
                } else if (line.startsWith("Bot: ")) {
                    addBotMessage(line);
                }
            }
        } catch (IOException e) {
            LOGGER.info("No chat history found");
        }
    }

    /**
     * Saves message to history.
     */
    private void saveChatHistory(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save chat history", e);
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            writer.write("");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to clear chat history", e);
        }
        addBotMessage("Bot: Chat cleared. Ask me anything!");
    }

    /**
     * Adds bot message (left).
     */
    private void addBotMessage(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("bot-message");
        label.setWrapText(true);
        label.setMaxWidth(500);

        HBox hbox = new HBox(label);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hbox.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        Platform.runLater(() -> chatContainer.getChildren().add(hbox));
    }

    /**
     * Adds user message (right).
     */
    private void addUserMessage(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("user-message");
        label.setWrapText(true);
        label.setMaxWidth(500);

        HBox hbox = new HBox(label);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        hbox.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

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
     * Queries Ollama API.
     */
    private String queryOllama(String input) throws IOException {
        JSONObject payload = new JSONObject()
                .put("model", ollamaModel)
                .put("prompt", input)
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