package elite.companion.comms;

import com.google.cloud.speech.v1.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.eventbus.EventBus;
import com.google.protobuf.ByteString;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.model.VoiceCommandDTO;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Scanner;

import static elite.companion.Globals.GOOGLE_API_KEY;

public class VoiceCommandModule {

    private SpeechClient speechClient;
    private final EventBus bus;
    private final String xaiApiKey = "your-xai-api-key";

    public VoiceCommandModule(EventBus bus) throws IOException {
        this.bus = bus;
        try (InputStream sttStream = getClass().getResourceAsStream(GOOGLE_API_KEY)) {
            if (sttStream == null) {
                throw new IOException("STT service account JSON file '" + GOOGLE_API_KEY + "' not found in resources");
            }
            GoogleCredentials sttCredentials = GoogleCredentials.fromStream(sttStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
            SpeechSettings settings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> sttCredentials)
                    .build();
            speechClient = SpeechClient.create(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(this::listen).start();
    }

    private void listen() {
        while (true) {
            try {
                byte[] audioData = recordAudio(); // Capture audio from your mic
                RecognizeResponse response = recognizeSpeech(audioData);
                String transcript = response.getResults(0).getAlternatives(0).getTranscript();
                System.out.println("Heard: " + transcript);
/*
                if (transcript != null && !transcript.isEmpty()) {
                    processCommand(transcript);
                }
*/
            } catch (Exception e) {
                System.err.println("STT error: " + e.getMessage());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private byte[] recordAudio() throws Exception {
        // Optimized for your high-end audio setup (Shure SM7B, RME Fireface UFX+)
        javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(24000, 16, 1, true, false); // Match your TTS rate
        javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, format);
        javax.sound.sampled.TargetDataLine line = (javax.sound.sampled.TargetDataLine) javax.sound.sampled.AudioSystem.getLine(info);
        line.open(format);
        line.start();
        byte[] data = new byte[24000 * 5]; // 5 seconds at 24kHz
        int bytesRead = line.read(data, 0, data.length);
        byte[] trimmedData = new byte[bytesRead];
        System.arraycopy(data, 0, trimmedData, 0, bytesRead);
        line.stop();
        line.close();
        return trimmedData;
    }

    private RecognizeResponse recognizeSpeech(byte[] audioData) throws Exception {
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(24000) // Match your audio setup
                .setLanguageCode("en-US")
                .setEnableAutomaticPunctuation(true) // Improve natural parsing
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(audioData))
                .build();
        return speechClient.recognize(config, audio);
    }

    private void processCommand(String transcribedText) {
        String prompt = "Interpret this Elite Dangerous app command: '" + transcribedText +
                "'. Output JSON: {'action': 'set_mining_target', 'target': 'Tritium'} or similar.";
        String response = callXaiApi(prompt);

        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        String target = json.has("target") ? json.get("target").getAsString() : null;

        VoiceCommandDTO dto = new VoiceCommandDTO(Instant.now().toString(), transcribedText);
        dto.setInterpretedAction(action);
        if (target != null) dto.setParams(target);
        bus.post(dto);
    }

    private String callXaiApi(String prompt) {
        try {
            URL url = new URL("https://api.x.ai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + xaiApiKey);
            conn.setDoOutput(true);

            String body = "{\"model\": \"grok\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            try (var os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            if (conn.getResponseCode() == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    String response = scanner.useDelimiter("\\A").next();
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    return json.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .get("message").getAsJsonObject()
                            .get("content").getAsString();
                }
            } else {
                System.err.println("xAI API error: " + conn.getResponseCode());
                return "{}";
            }
        } catch (Exception e) {
            System.err.println("API call error: " + e.getMessage());
            return "{}";
        }
    }
}