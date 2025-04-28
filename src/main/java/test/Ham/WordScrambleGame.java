package test.Ham;

import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Random;

public class WordScrambleGame {
    private static final String RANDOM_WORD_API = "https://random-word-api.herokuapp.com/word?number=10";
    private static final String URBAN_DICT_API = "https://urban-dictionary7.p.rapidapi.com/v0/define?term=%s";
    private static final String RAPIDAPI_KEY = "cfb93d0783msh62e487236d8f7d2p160c7ejsneed76ee7abb9";
    private static final String RAPIDAPI_HOST = "urban-dictionary7.p.rapidapi.com";
    private final OkHttpClient client = new OkHttpClient();
    private final Random random = new Random();

    public static class WordData {
        public final String originalWord;
        public final String scrambledWord;
        public final String meaning;

        public WordData(String originalWord, String scrambledWord, String meaning) {
            this.originalWord = originalWord;
            this.scrambledWord = scrambledWord;
            this.meaning = meaning;
        }
    }

    public WordData getNewWord() {
        String word = fetchRandomWord();
        String meaning = fetchWordMeaning(word);
        String scrambledWord = scrambleWord(word);
        System.out.println("New word: " + word + ", Scrambled: " + scrambledWord + ", Meaning: " + meaning);
        return new WordData(word, scrambledWord, meaning);
    }

    private String fetchRandomWord() {
        try {
            Request request = new Request.Builder()
                    .url(RANDOM_WORD_API)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Random Word API failed: " + response.code());
                    return "apple";
                }
                String json = response.body().string();
                JSONArray words = new JSONArray(json);
                for (int i = 0; i < words.length(); i++) {
                    String word = words.getString(i);
                    if (word.length() >= 3 && word.length() <= 5) {
                        return word;
                    }
                }
            }
        } catch (IOException | org.json.JSONException e) {
            System.out.println("Error fetching random word: " + e.getMessage());
        }
        System.out.println("Using fallback word: apple");
        return "apple";
    }

    private String fetchWordMeaning(String word) {
        try {
            Request request = new Request.Builder()
                    .url(String.format(URBAN_DICT_API, word))
                    .addHeader("X-RapidAPI-Key", RAPIDAPI_KEY)
                    .addHeader("X-RapidAPI-Host", RAPIDAPI_HOST)
                    .build();
            try (Response response = client.newCall(request).execute ()) {
                if (!response.isSuccessful()) {
                    System.out.println("Urban Dictionary API failed: " + response.code());
                    return "No meaning available";
                }
                String json = response.body().string();
                JSONObject data = new JSONObject(json);
                JSONArray definitions = data.getJSONArray("list");
                if (definitions.length() > 0) {
                    return definitions.getJSONObject(0).getString("definition");
                }
            }
        } catch (IOException | org.json.JSONException e) {
            System.out.println("Error fetching meaning for " + word + ": " + e.getMessage());
        }
        return "No meaning available";
    }

    private String scrambleWord(String word) {
        char[] chars = word.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}