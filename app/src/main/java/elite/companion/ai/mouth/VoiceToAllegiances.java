package elite.companion.ai.mouth;

import elite.companion.ai.brain.AICadence;

import java.util.*;
import java.util.stream.Collectors;

public class VoiceToAllegiances {
    private static final VoiceToAllegiances instance = new VoiceToAllegiances();
    private final Map<AICadence, List<AiVoices>> cadenceToVoices;

    public static VoiceToAllegiances getInstance() {
        return instance;
    }

    private VoiceToAllegiances() {
        cadenceToVoices = new EnumMap<>(AICadence.class);
        cadenceToVoices.put(AICadence.IMPERIAL, Arrays.asList(
                AiVoices.CHARLES, // en-GB-Chirp3-HD-Algenib
                AiVoices.ANNA,    // en-GB-Chirp-HD-F
                AiVoices.MARY,    // en-GB-Neural2-A
                AiVoices.BETTY,   // en-GB-Chirp3-HD-Aoede
                AiVoices.OLIVIA   // en-GB-Chirp3-HD-Aoede
        ));
        cadenceToVoices.put(AICadence.FEDERATION, Arrays.asList(
                AiVoices.MICHAEL, // en-US-Chirp3-HD-Charon
                AiVoices.STEVE,   // en-US-Chirp3-HD-Algenib
                AiVoices.JOSEPH,  // en-US-Chirp3-HD-Sadachbia
                AiVoices.JENNIFER,// en-US-Chirp3-HD-Sulafat
                AiVoices.RACHEL,  // en-US-Chirp3-HD-Zephyr
                AiVoices.KAREN,   // en-US-Chirp3-HD-Despina
                AiVoices.EMMA     // en-US-Chirp3-HD-Despina
        ));
        cadenceToVoices.put(AICadence.ALLIANCE, Arrays.asList(
                AiVoices.CHARLES, // en-GB-Chirp3-HD-Algenib (British)
                AiVoices.STEVE,   // en-US-Chirp3-HD-Algenib (US)
                AiVoices.RACHEL,  // en-US-Chirp3-HD-Zephyr (US, diverse)
                AiVoices.MARY     // en-GB-Neural2-A (British)
        ));
    }

    public AiVoices getVoiceForCadence(AICadence cadence, AiVoices currentVoice) {
        List<AiVoices> voices = cadenceToVoices.getOrDefault(cadence, List.of());
        boolean isMale = currentVoice.isMale();
        Random random = new Random();

        List<AiVoices> matchingVoices = voices.stream()
                .filter(voice -> !voice.equals(currentVoice))
                .filter(voice -> voice.isMale() == isMale)
                .collect(Collectors.toList());

        return matchingVoices.isEmpty() ? currentVoice
                : matchingVoices.get(random.nextInt(matchingVoices.size()));
    }
}