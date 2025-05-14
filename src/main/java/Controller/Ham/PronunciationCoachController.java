package Controller.Ham;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import okhttp3.*;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class PronunciationCoachController {

    @FXML private TextField wordInput;
    @FXML private Button listenBtn;
    @FXML private Button recordBtn;
    @FXML private Label chatMessage;
    @FXML private ImageView mascot;
    @FXML private VBox chatBox;
    @FXML private MediaView audioPlayer;
    @FXML private Rectangle balloon1, balloon2, balloon3;
    @FXML private Rectangle star1, star2, star3, star4, star5, star6, star7, star8, star9, star10;
    @FXML private Rectangle confetti1, confetti2, confetti3;

    private String azureKey;
    private String azureRegion;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private File tempAudioFile;
    private long recordingStartTime;
    private TargetDataLine currentLine; // Track the current line for cleanup
    private AudioInputStream audioInputStream; // Track AudioInputStream for explicit closure
    private final String[] messages = {
            "Let’s say %s together!",
            "Squawk! Here’s how to say %s!",
            "Listen up, it’s %s time!",
            "Pronto says: %s!",
            "Flap your wings and say %s!",
            "Get ready to squawk %s with me!",
            "Chirp chirp, it’s %s!",
            "Say %s like a parrot pro!"
    };
    private final String[] waitingMessages = {
            "Squawk! Checking your voice with my parrot powers!",
            "Bip Bap Boop! One sec, the robot’s listening hard!",
            "Flap flap! Pronto’s sending your word to the sky!",
            "Squawk-a-doodle! Hold on, I’m decoding your squawk!",
            "Chirp chirp! My robot pal’s crunching the sounds!",
            "Bawk bawk! Give me a wingbeat to check it out!",
            "Squawk! Feathers crossed, let’s see what you said!",
            "Zoom zoom! Pronto’s robot crew is on the case!"
    };

    @FXML
    public void initialize() {
        System.out.println("DEBUG: Initializing PronunciationCoachController");
        // Load Azure credentials
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input == null) {
                System.out.println("DEBUG: config.properties not found in resources");
                chatMessage.setText("Squawk! Config file missing!");
                return;
            }
            Properties prop = new Properties();
            prop.load(input);
            azureKey = prop.getProperty("azure.speech.key");
            azureRegion = prop.getProperty("azure.speech.region");
            System.out.println("DEBUG: Azure key loaded: " + (azureKey != null ? "[REDACTED]" : "null"));
            System.out.println("DEBUG: Azure region: " + azureRegion);
            if (azureKey == null || azureRegion == null) {
                chatMessage.setText("Squawk! Azure credentials missing in config!");
            }
        } catch (IOException e) {
            System.out.println("DEBUG: Failed to load config.properties: " + e.getMessage());
            chatMessage.setText("Squawk! Error loading config!");
            e.printStackTrace();
        }

        // Set mascot image
        try {
            Image image = new Image(getClass().getResourceAsStream("/assets/images/pronto-parrot.gif"));
            mascot.setImage(image);
            System.out.println("DEBUG: Mascot image loaded successfully");
        } catch (Exception e) {
            System.out.println("DEBUG: Failed to load mascot image: " + e.getMessage());
            chatMessage.setText("Squawk! Couldn’t load Pronto’s image!");
            e.printStackTrace();
        }

        // Initialize audio recording
        updateTempAudioFile();
        System.out.println("DEBUG: Temp audio file path: " + tempAudioFile.getAbsolutePath());

        // Start background animations
        startBackgroundAnimations();
        System.out.println("DEBUG: Background animations started");

        // Debug FXML injections
        System.out.println("DEBUG: chatBox initialized: " + (chatBox != null));
        System.out.println("DEBUG: mascot initialized: " + (mascot != null));
        System.out.println("DEBUG: wordInput initialized: " + (wordInput != null));
        System.out.println("DEBUG: listenBtn initialized: " + (listenBtn != null));
        System.out.println("DEBUG: recordBtn initialized: " + (recordBtn != null));

        // Test audio formats
        System.out.println("DEBUG: Calling testFormats");
        testFormats();
    }

    private void updateTempAudioFile() {
        tempAudioFile = new File("audio/temp_recording_" + System.currentTimeMillis() + ".wav");
    }

    private void startBackgroundAnimations() {
        System.out.println("DEBUG: Starting background animations");
        // Balloons float
        for (Rectangle balloon : new Rectangle[]{balloon1, balloon2, balloon3}) {
            if (balloon != null) {
                TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(6), balloon);
                floatAnim.setFromY(0);
                floatAnim.setToY(-20);
                floatAnim.setCycleCount(Animation.INDEFINITE);
                floatAnim.setAutoReverse(true);
                floatAnim.play();
            } else {
                System.out.println("DEBUG: Balloon is null");
            }
        }

        // Stars fall and reset
        for (Rectangle star : new Rectangle[]{star1, star2, star3, star4, star5, star6, star7, star8, star9, star10}) {
            if (star != null) {
                TranslateTransition fallAnim = new TranslateTransition(Duration.seconds(6), star);
                fallAnim.setFromY(-100);
                fallAnim.setToY(600);
                fallAnim.setCycleCount(Animation.INDEFINITE);
                fallAnim.setOnFinished(e -> star.setTranslateY(-100));
                fallAnim.play();
            } else {
                System.out.println("DEBUG: Star is null");
            }
        }

        // Confetti falls and resets
        for (Rectangle confetti : new Rectangle[]{confetti1, confetti2, confetti3}) {
            if (confetti != null) {
                TranslateTransition fallAnim = new TranslateTransition(Duration.seconds(5), confetti);
                fallAnim.setFromY(-100);
                fallAnim.setToY(600);
                fallAnim.setCycleCount(Animation.INDEFINITE);
                fallAnim.setOnFinished(e -> confetti.setTranslateY(-100));
                fallAnim.play();
            } else {
                System.out.println("DEBUG: Confetti is null");
            }
        }
    }

    private void animateMascot() {
        Platform.runLater(() -> {
            System.out.println("DEBUG: Animating mascot, chatBox: " + (chatBox != null) + ", mascot: " + (mascot != null));
            if (mascot == null || chatBox == null) {
                System.out.println("DEBUG: Skipping animation due to null mascot or chatBox");
                return;
            }
            mascot.getStyleClass().add("talking");
            chatBox.getStyleClass().add("active");
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                mascot.getStyleClass().remove("talking");
                chatBox.getStyleClass().remove("active");
                System.out.println("DEBUG: Mascot animation ended");
            });
            pause.play();
        });
    }

    @FXML
    private void handleListen() {
        System.out.println("DEBUG: handleListen called");
        String word = wordInput.getText().trim();
        System.out.println("DEBUG: Input word: " + word);
        if (word.isEmpty()) {
            System.out.println("DEBUG: No word entered");
            chatMessage.setText("Squawk! Please type a word first!");
            animateMascot();
            return;
        }

        chatMessage.setText("Squawk! Generating audio...");
        animateMascot();

        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("DEBUG: Generating TTS for word: " + word);
                String message = String.format(messages[new Random().nextInt(messages.length)], word);
                System.out.println("DEBUG: TTS message: " + message);
                String audioUrl = generateTTS(message);
                System.out.println("DEBUG: Audio file generated: " + audioUrl);
                Platform.runLater(() -> {
                    try {
                        System.out.println("DEBUG: Loading media: " + audioUrl);
                        Media media = new Media(new File(audioUrl).toURI().toString());
                        if (mediaPlayer != null) {
                            System.out.println("DEBUG: Stopping existing mediaPlayer");
                            mediaPlayer.stop();
                        }
                        mediaPlayer = new MediaPlayer(media);
                        audioPlayer.setMediaPlayer(mediaPlayer);
                        System.out.println("DEBUG: Playing audio");
                        mediaPlayer.play();
                        chatMessage.setText(message);
                        animateMascot();
                    } catch (Exception e) {
                        System.out.println("DEBUG: Failed to play audio: " + e.getMessage());
                        chatMessage.setText("Squawk! Couldn’t play the audio: " + e.getMessage());
                        animateMascot();
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.out.println("DEBUG: TTS generation failed: " + e.getMessage());
                Platform.runLater(() -> {
                    chatMessage.setText("Squawk! Couldn’t generate audio: " + e.getMessage());
                    animateMascot();
                });
                e.printStackTrace();
            }
        });
    }

    private String generateTTS(String text) throws IOException {
        System.out.println("DEBUG: generateTTS called with text: " + text);
        if (azureKey == null || azureRegion == null) {
            System.out.println("DEBUG: Azure credentials missing");
            throw new IOException("Azure credentials not initialized");
        }

        OkHttpClient client = new OkHttpClient();
        String ssml = String.format(
                "<speak version='1.0' xml:lang='en-US'><voice xml:lang='en-US' name='en-US-DavisNeural' style='cheerful'><prosody pitch='+10%%' rate='1.1'>%s</prosody></voice></speak>",
                text
        );
        System.out.println("DEBUG: SSML: " + ssml);

        RequestBody body = RequestBody.create(ssml, MediaType.parse("application/ssml+xml"));
        Request request = new Request.Builder()
                .url("https://" + azureRegion + ".tts.speech.microsoft.com/cognitiveservices/v1")
                .addHeader("Ocp-Apim-Subscription-Key", azureKey)
                .addHeader("Content-Type", "application/ssml+xml")
                .addHeader("X-Microsoft-OutputFormat", "audio-48khz-192kbitrate-mono-mp3")
                .post(body)
                .build();
        System.out.println("DEBUG: Sending TTS request to: " + request.url());

        try (Response response = client.newCall(request).execute()) {
            System.out.println("DEBUG: TTS response code: " + response.code());
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                System.out.println("DEBUG: TTS response body: " + responseBody);
                throw new IOException("TTS request failed: " + response.code() + " - " + responseBody);
            }
            String fileName = System.currentTimeMillis() + ".mp3";
            Path filePath = Paths.get("audio/" + fileName);
            System.out.println("DEBUG: Writing audio to: " + filePath);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, response.body().bytes());
            System.out.println("DEBUG: Audio file written successfully");
            return filePath.toString();
        }
    }

    @FXML
    private void handleRecord() {
        System.out.println("DEBUG: handleRecord called, isRecording: " + isRecording);
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
            Platform.runLater(() -> recordBtn.setText("Record My Try"));
        }
    }

    private void startRecording() {
        System.out.println("DEBUG: startRecording called");
        try {
            // Log available mixers
            System.out.println("DEBUG: Available mixers:");
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            for (Mixer.Info mixerInfo : mixerInfos) {
                System.out.println("DEBUG: Mixer: " + mixerInfo.getName() + ", " + mixerInfo.getDescription());
            }

            // Try 44.1 kHz first, then 16 kHz
            AudioFormat[] formats = {
                    new AudioFormat(44100, 16, 1, true, false),
                    new AudioFormat(16000, 16, 1, true, false)
            };
            TargetDataLine line = null;
            AudioFormat selectedFormat = null;

            // Select USB microphone mixer
            Mixer mixer = null;
            for (Mixer.Info mixerInfo : mixerInfos) {
                if (mixerInfo.getName().contains("Microphone (3- USB Audio Device)")) {
                    mixer = AudioSystem.getMixer(mixerInfo);
                    System.out.println("DEBUG: Selected mixer: " + mixerInfo.getName());
                    break;
                }
            }
            if (mixer == null) {
                System.out.println("DEBUG: USB microphone not found, using default mixer");
                mixer = AudioSystem.getMixer(null); // Default mixer
            }

            // Try each format
            for (AudioFormat format : formats) {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                System.out.println("DEBUG: Attempting format: " + format);
                for (int attempt = 1; attempt <= 3; attempt++) {
                    try {
                        line = (TargetDataLine) mixer.getLine(info);
                        System.out.println("DEBUG: Opening microphone, attempt: " + attempt + ", format: " + format);
                        line.open(format);
                        selectedFormat = format;
                        break;
                    } catch (LineUnavailableException e) {
                        System.out.println("DEBUG: Attempt " + attempt + " failed for format " + format + ": " + e.getMessage());
                        if (attempt < 3) {
                            Thread.sleep(1000); // Increased delay
                        }
                    }
                }
                if (line != null) break;
            }

            if (line == null) {
                throw new LineUnavailableException("Failed to open microphone after trying all formats");
            }

            currentLine = line;
            currentLine.start();
            System.out.println("DEBUG: Microphone opened with format: " + selectedFormat);

            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            updateTempAudioFile(); // Set new temp file name
            System.out.println("DEBUG: Writing WAV to: " + tempAudioFile.getAbsolutePath());
            audioInputStream = new AudioInputStream(currentLine);
            CompletableFuture.runAsync(() -> {
                try {
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, tempAudioFile);
                    System.out.println("DEBUG: Recording stopped, WAV file written");
                } catch (IOException e) {
                    System.out.println("DEBUG: Recording I/O error: " + e.getMessage());
                    Platform.runLater(() -> {
                        chatMessage.setText("Squawk! Recording failed: " + e.getMessage());
                        animateMascot();
                    });
                    e.printStackTrace();
                } finally {
                    cleanupAudioResources();
                }
            });
            Platform.runLater(() -> {
                recordBtn.setText("Stop Recording");
                chatMessage.setText("Squawk! Recording... Say the word!");
                animateMascot();
            });
        } catch (LineUnavailableException e) {
            System.out.println("DEBUG: Microphone access denied: " + e.getMessage());
            Platform.runLater(() -> {
                isRecording = false;
                recordBtn.setText("Record My Try");
                chatMessage.setText("Squawk! I need microphone access to hear you!");
                animateMascot();
            });
            cleanupAudioResources();
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("DEBUG: Unexpected error in startRecording: " + e.getMessage());
            Platform.runLater(() -> {
                isRecording = false;
                recordBtn.setText("Record My Try");
                chatMessage.setText("Squawk! Recording setup failed!");
                animateMascot();
            });
            cleanupAudioResources();
            e.printStackTrace();
        }
    }

    private void cleanupAudioResources() {
        System.out.println("DEBUG: Cleaning up audio resources");
        if (audioInputStream != null) {
            try {
                audioInputStream.close();
                System.out.println("DEBUG: AudioInputStream closed");
            } catch (IOException e) {
                System.out.println("DEBUG: Failed to close AudioInputStream: " + e.getMessage());
            }
            audioInputStream = null;
        }
        if (currentLine != null) {
            currentLine.stop();
            currentLine.close();
            currentLine = null;
            System.out.println("DEBUG: Microphone line closed");
        }
        // Close all mixers to release resources
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (mixer.isOpen()) {
                mixer.close();
                System.out.println("DEBUG: Closed mixer: " + mixerInfo.getName());
            }
        }
    }

    private void stopRecording() {
        System.out.println("DEBUG: stopRecording called");
        isRecording = false;
        long recordingDuration = System.currentTimeMillis() - recordingStartTime;
        System.out.println("DEBUG: Recording duration: " + recordingDuration + " ms");

        // Copy temp file early to avoid locking
        File tempCopy = new File("audio/temp_copy_" + System.currentTimeMillis() + ".wav");
        try {
            Files.copy(tempAudioFile.toPath(), tempCopy.toPath());
            System.out.println("DEBUG: Copied temp file to: " + tempCopy.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("DEBUG: Failed to copy temp file: " + e.getMessage());
        }

        // Cleanup audio resources
        cleanupAudioResources();

        String word = wordInput.getText().trim();
        System.out.println("DEBUG: Input word for STT: " + word);
        if (word.isEmpty()) {
            System.out.println("DEBUG: No word entered for STT");
            Platform.runLater(() -> {
                chatMessage.setText("Squawk! Please type a word first!");
                animateMascot();
            });
            try {
                Files.deleteIfExists(tempAudioFile.toPath());
                Files.deleteIfExists(tempCopy.toPath());
            } catch (IOException e) {
                System.out.println("DEBUG: Failed to delete files: " + e.getMessage());
            }
            return;
        }

        // Validate recorded file
        if (!tempAudioFile.exists() || tempAudioFile.length() < 44) { // Minimum WAV header size
            System.out.println("DEBUG: Invalid or empty recording: exists=" + tempAudioFile.exists() + ", size=" + tempAudioFile.length());
            Platform.runLater(() -> {
                chatMessage.setText("Squawk! Recording is empty or invalid!");
                animateMascot();
            });
            try {
                Files.deleteIfExists(tempAudioFile.toPath());
                Files.deleteIfExists(tempCopy.toPath());
            } catch (IOException e) {
                System.out.println("DEBUG: Failed to delete invalid temp WAV: " + e.getMessage());
            }
            return;
        }
        System.out.println("DEBUG: Recorded file size: " + tempAudioFile.length() + " bytes");

        String randomMessage = waitingMessages[new Random().nextInt(waitingMessages.length)];
        Platform.runLater(() -> {
            chatMessage.setText(randomMessage);
            animateMascot();
        });

        CompletableFuture.runAsync(() -> {
            Process process = null;
            try {
                System.out.println("DEBUG: Starting FFmpeg conversion");
                String outputWav = "audio/" + System.currentTimeMillis() + ".wav";
                System.out.println("DEBUG: FFmpeg output file: " + outputWav);
                ProcessBuilder pb = new ProcessBuilder(
                        "C:\\ffmpeg\\ffmpeg.exe", "-i", tempCopy.getAbsolutePath(),
                        "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1", outputWav
                );
                pb.redirectErrorStream(true); // Merge stderr into stdout
                System.out.println("DEBUG: FFmpeg command: " + String.join(" ", pb.command()));
                process = pb.start();

                // Capture FFmpeg output
                StringBuilder ffmpegOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        ffmpegOutput.append(line).append("\n");
                    }
                }
                int exitCode = process.waitFor();
                System.out.println("DEBUG: FFmpeg exit code: " + exitCode);
                System.out.println("DEBUG: FFmpeg output: " + ffmpegOutput);
                if (exitCode != 0) {
                    throw new IOException("FFmpeg conversion failed with exit code " + exitCode + ": " + ffmpegOutput);
                }

                System.out.println("DEBUG: Analyzing STT");
                String feedback = analyzeSTT(word, outputWav);
                System.out.println("DEBUG: STT feedback: " + feedback);
                Platform.runLater(() -> {
                    chatMessage.setText(feedback);
                    animateMascot();
                });
                System.out.println("DEBUG: Deleting output WAV: " + outputWav);
                Files.deleteIfExists(Paths.get(outputWav));
                System.out.println("DEBUG: Deleting temp copy: " + tempCopy.getAbsolutePath());
                Files.deleteIfExists(tempCopy.toPath());
            } catch (Exception e) {
                System.out.println("DEBUG: STT processing failed: " + e.getMessage());
                Platform.runLater(() -> {
                    chatMessage.setText("Squawk! Couldn’t analyze your voice: " + e.getMessage());
                    animateMascot();
                });
                e.printStackTrace();
            } finally {
                // Ensure FFmpeg process is terminated
                if (process != null) {
                    process.destroy();
                    try {
                        process.waitFor(1, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("DEBUG: FFmpeg process terminated");
                }
                // Retry deleting temp WAV
                System.out.println("DEBUG: Attempting to delete temp WAV: " + tempAudioFile.getAbsolutePath());
                for (int i = 0; i < 5; i++) {
                    try {
                        Files.deleteIfExists(tempAudioFile.toPath());
                        System.out.println("DEBUG: Temp WAV deleted successfully");
                        break;
                    } catch (IOException e) {
                        System.out.println("DEBUG: Retry " + (i + 1) + ": Failed to delete temp WAV: " + e.getMessage());
                        try {
                            Thread.sleep(2000); // Increased delay
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        });
    }

    private String analyzeSTT(String targetWord, String wavFile) throws Exception {
        System.out.println("DEBUG: analyzeSTT called with targetWord: " + targetWord + ", wavFile: " + wavFile);
        if (azureKey == null || azureRegion == null) {
            System.out.println("DEBUG: Azure credentials missing in analyzeSTT");
            throw new IllegalStateException("Azure credentials not initialized");
        }

        SpeechConfig config = SpeechConfig.fromSubscription(azureKey, azureRegion);
        System.out.println("DEBUG: SpeechConfig created");
        AudioConfig audioInput = AudioConfig.fromWavFileInput(wavFile);
        System.out.println("DEBUG: AudioConfig created for: " + wavFile);
        try (SpeechRecognizer recognizer = new SpeechRecognizer(config, audioInput)) {
            System.out.println("DEBUG: Starting speech recognition");
            SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();
            System.out.println("DEBUG: Speech recognition result: " + result.getText());
            String recognizedText = result.getText().toLowerCase().replaceAll("[^a-zA-Z ]", "");
            System.out.println("DEBUG: Processed recognized text: " + recognizedText);
            targetWord = targetWord.toLowerCase();
            if (recognizedText.equals(targetWord)) {
                return "Awesome! You nailed '" + targetWord + "' perfectly!";
            } else {
                return "Good try! I heard '" + recognizedText + "'. Let’s say '" + targetWord + "' like this: " +
                        String.join("-", targetWord.split(""));
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Speech recognition failed: " + e.getMessage());
            throw e;
        } finally {
            config.close();
            audioInput.close();
        }
    }

    // Test method for debugging audio formats
    public void testFormats() {
        System.out.println("DEBUG: testFormats called");
        AudioFormat[] formats = {
                new AudioFormat(16000, 16, 1, true, false),
                new AudioFormat(44100, 16, 1, true, false),
                new AudioFormat(48000, 16, 1, true, false)
        };
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            System.out.println("DEBUG: Testing mixer: " + mixerInfo.getName());
            for (AudioFormat format : formats) {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                boolean supported = mixer.isLineSupported(info);
                System.out.println("DEBUG: Format " + format + " supported: " + supported);
                if (supported) {
                    try (TargetDataLine line = (TargetDataLine) mixer.getLine(info)) {
                        line.open(format);
                        System.out.println("DEBUG: Format " + format + " opened successfully");
                        line.close();
                    } catch (LineUnavailableException e) {
                        System.out.println("DEBUG: Format " + format + " failed to open: " + e.getMessage());
                    }
                }
            }
        }
    }
}
