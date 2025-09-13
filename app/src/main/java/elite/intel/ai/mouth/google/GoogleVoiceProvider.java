package elite.intel.ai.mouth.google;

import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.session.SystemSession;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Singleton class responsible for managing Google TTS voice mappings and selection logic.
 * Maps AiVoices enum names (e.g., "Jennifer") to Google VoiceSelectionParams and provides
 * methods to retrieve user-selected and random voices dynamically as AiVoices enums.
 */
public class GoogleVoiceProvider {
    private static final GoogleVoiceProvider INSTANCE = new GoogleVoiceProvider();
    private final Map<String, VoiceSelectionParams> voiceMap;

    private GoogleVoiceProvider() {
        // Initialize voice mappings using AiVoices names
        voiceMap = new HashMap<>();
        voiceMap.put(AiVoices.ANNA.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp-HD-F").build());
        voiceMap.put(AiVoices.BETTY.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Aoede").build());
        voiceMap.put(AiVoices.CHARLES.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Algenib").build());
        voiceMap.put(AiVoices.EMMA.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Despina").build());
        voiceMap.put(AiVoices.JAKE.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Iapetus").build());
        voiceMap.put(AiVoices.JAMES.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-AU-Chirp3-HD-Algieba").build());
        voiceMap.put(AiVoices.JENNIFER.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Sulafat").build());
        voiceMap.put(AiVoices.JOSEPH.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Sadachbia").build());
        voiceMap.put(AiVoices.KAREN.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Zephyr").build());
        voiceMap.put(AiVoices.MARY.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Neural2-A").build());
        voiceMap.put(AiVoices.MICHAEL.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Charon").build());
        voiceMap.put(AiVoices.OLIVIA.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Aoede").build());
        voiceMap.put(AiVoices.RACHEL.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Zephyr").build());
        voiceMap.put(AiVoices.STEVE.getName(), VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Algenib").build());
    }

    public static GoogleVoiceProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves the user-selected AI voice as an AiVoices enum.
     *
     * @return AiVoices for the current AI voice, or default (Jennifer) if none selected.
     */
    public AiVoices getUserSelectedVoice() {
        AiVoices aiVoice = SystemSession.getInstance().getAIVoice();
        return aiVoice != null ? aiVoice : AiVoices.JENNIFER; // Default to Jennifer
    }

    /**
     * Retrieves a random AiVoices enum value, excluding the user-selected AI voice.
     *
     * @return AiVoices for a random voice, or default (Jennifer) if none available.
     */
    public AiVoices getRandomVoice() {
        AiVoices currentAiVoice = SystemSession.getInstance().getAIVoice();
        AiVoices[] availableVoices = Arrays.stream(AiVoices.values())
                .filter(voice -> !voice.getName().equals(currentAiVoice.getName()))
                .toArray(AiVoices[]::new);
        if (availableVoices.length == 0) {
            return AiVoices.JENNIFER; // Default to Jennifer
        }
        return availableVoices[new Random().nextInt(availableVoices.length)];
    }

    /**
     * Retrieves the speech rate for a given AiVoices voice name.
     *
     * @param voiceName The AiVoices enum name (e.g., "Jennifer").
     * @return The speech rate for the voice, or default (1.2) if not found.
     */
    public double getSpeechRate(String voiceName) {
        for (AiVoices voice : AiVoices.values()) {
            if (voice.getName().equals(voiceName)) {
                return voice.getSpeechRate();
            }
        }
        return 1.2; // Default speech rate
    }

    /**
     * Retrieves VoiceSelectionParams for a given AiVoices voice name.
     *
     * @param voiceName The AiVoices enum name (e.g., "Jennifer").
     * @return VoiceSelectionParams for the voice, or default (Jennifer) if not found.
     */
    public VoiceSelectionParams getVoiceParams(String voiceName) {
        VoiceSelectionParams params = voiceMap.get(voiceName);
        if (params == null) {
            params = voiceMap.get(AiVoices.JENNIFER.getName()); // Default to Jennifer
        }
        return params;
    }
}