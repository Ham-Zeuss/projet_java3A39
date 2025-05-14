package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PerspectiveApiService {
    private final OkHttpClient client;
    private final String apiKey;
    private final String apiUrl = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze";
    private final ObjectMapper objectMapper;
    private static final double TOXICITY_THRESHOLD = 0.7;

    public PerspectiveApiService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        System.out.println("Jackson Databind Version: " + com.fasterxml.jackson.databind.ObjectMapper.class.getPackage().getImplementationVersion());
        System.out.println("Jackson Core Version: " + com.fasterxml.jackson.core.JsonParser.class.getPackage().getImplementationVersion());
    }

    public CompletableFuture<Double> analyzeTextAsync(String text, String language) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        // Validate inputs
        if (text == null || text.trim().isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("Text cannot be null or empty"));
            return future;
        }
        if (language == null || language.trim().isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("Language cannot be null or empty"));
            return future;
        }

        // Build JSON payload
        String jsonPayload;
        try {
            Map<String, Object> payload = new HashMap<>();
            Map<String, String> comment = new HashMap<>();
            comment.put("text", text);
            payload.put("comment", comment);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("TOXICITY", new HashMap<>());
            payload.put("requestedAttributes", attributes);
            payload.put("languages", new String[]{language});
            jsonPayload = objectMapper.writeValueAsString(payload);
            System.out.println("Request Payload: " + jsonPayload);
        } catch (Exception e) {
            e.printStackTrace();
            future.completeExceptionally(new IOException("Failed to serialize JSON payload", e));
            return future;
        }

        // Create request
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        String urlWithKey = apiUrl + "?key=" + apiKey;
        Request request = new Request.Builder()
                .url(urlWithKey)
                .post(body)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "No response body";
                        System.out.println("API Error Response: " + responseBody);
                        future.completeExceptionally(new IOException("API request failed with code " + response.code() + ": " + responseBody));
                        return;
                    }

                    String responseBody = response.body().string();
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    double score = jsonNode
                            .path("attributeScores")
                            .path("TOXICITY")
                            .path("summaryScore")
                            .path("value")
                            .asDouble();
                    System.out.println("Toxicity Score: " + score);
                    future.complete(score);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }

    public CompletableFuture<Boolean> isToxicAsync(String text, String language) {
        return analyzeTextAsync(text, language)
                .thenApply(score -> score > TOXICITY_THRESHOLD);
    }
}