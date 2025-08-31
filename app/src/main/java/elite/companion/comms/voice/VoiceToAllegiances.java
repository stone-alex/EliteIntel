package elite.companion.comms.voice;

import elite.companion.comms.ai.AICadence;

import java.util.*;
import java.util.stream.Collectors;

public class VoiceToAllegiances {
    private static final VoiceToAllegiances instance = new VoiceToAllegiances();
    private final Map<AICadence, List<Voices>> cadenceToVoices;

    public static VoiceToAllegiances getInstance() {
        return instance;
    }

    private VoiceToAllegiances() {
        cadenceToVoices = new EnumMap<>(AICadence.class);
        cadenceToVoices.put(AICadence.IMPERIAL, Arrays.asList(
                Voices.CHARLES, // en-GB-Chirp3-HD-Algenib
                Voices.ANNA,    // en-GB-Chirp-HD-F
                Voices.MARY,    // en-GB-Neural2-A
                Voices.BETTY,   // en-GB-Chirp3-HD-Aoede
                Voices.OLIVIA   // en-GB-Chirp3-HD-Aoede
        ));
        cadenceToVoices.put(AICadence.FEDERATION, Arrays.asList(
                Voices.MICHAEL, // en-US-Chirp3-HD-Charon
                Voices.STEVE,   // en-US-Chirp3-HD-Algenib
                Voices.JOSEPH,  // en-US-Chirp3-HD-Sadachbia
                Voices.JENNIFER,// en-US-Chirp3-HD-Sulafat
                Voices.RACHEL,  // en-US-Chirp3-HD-Zephyr
                Voices.KAREN,   // en-US-Chirp3-HD-Despina
                Voices.EMMA     // en-US-Chirp3-HD-Despina
        ));
        cadenceToVoices.put(AICadence.ALLIANCE, Arrays.asList(
                Voices.CHARLES, // en-GB-Chirp3-HD-Algenib (British)
                Voices.STEVE,   // en-US-Chirp3-HD-Algenib (US)
                Voices.RACHEL,  // en-US-Chirp3-HD-Zephyr (US, diverse)
                Voices.MARY     // en-GB-Neural2-A (British)
        ));
    }

    public Voices getVoiceForCadence(AICadence cadence, Voices currentVoice) {
        List<Voices> voices = cadenceToVoices.getOrDefault(cadence, List.of());
        boolean isMale = currentVoice.isMale();
        Random random = new Random();

        List<Voices> matchingVoices = voices.stream()
                .filter(voice -> !voice.equals(currentVoice))
                .filter(voice -> voice.isMale() == isMale)
                .collect(Collectors.toList());

        return matchingVoices.isEmpty() ? currentVoice
                : matchingVoices.get(random.nextInt(matchingVoices.size()));
    }
}