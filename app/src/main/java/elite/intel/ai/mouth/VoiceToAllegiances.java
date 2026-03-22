package elite.intel.ai.mouth;

import elite.intel.ai.brain.ShipCadence;

import java.util.*;
import java.util.stream.Collectors;

public class VoiceToAllegiances {
    private static final VoiceToAllegiances instance = new VoiceToAllegiances();
    private final Map<ShipCadence, List<GoogleVoices>> cadenceToVoices;

    public static VoiceToAllegiances getInstance() {
        return instance;
    }

    private VoiceToAllegiances() {
        cadenceToVoices = new EnumMap<>(ShipCadence.class);
        cadenceToVoices.put(ShipCadence.IMPERIAL, Arrays.asList(
                //AiVoices.CHARLES, // en-GB-Chirp3-HD-Algenib
                GoogleVoices.ANNA,    // en-GB-Chirp-HD-F
                GoogleVoices.MARY,    // en-GB-Neural2-A
                //AiVoices.BETTY,   // en-GB-Chirp3-HD-Aoede
                GoogleVoices.OLIVIA   // en-GB-Chirp3-HD-Aoede
        ));
        cadenceToVoices.put(ShipCadence.FEDERATION, Arrays.asList(
                GoogleVoices.MICHAEL, // en-US-Chirp3-HD-Charon
                GoogleVoices.STEVE,   // en-US-Chirp3-HD-Algenib
                GoogleVoices.JOSEPH,  // en-US-Chirp3-HD-Sadachbia
                GoogleVoices.JENNIFER,// en-US-Chirp3-HD-Sulafat
                GoogleVoices.RACHEL,  // en-US-Chirp3-HD-Zephyr
                //AiVoices.KAREN,   // en-US-Chirp3-HD-Despina
                GoogleVoices.EMMA     // en-US-Chirp3-HD-Despina
        ));
        cadenceToVoices.put(ShipCadence.ALLIANCE, Arrays.asList(
                //AiVoices.CHARLES, // en-GB-Chirp3-HD-Algenib (British)
                GoogleVoices.STEVE,   // en-US-Chirp3-HD-Algenib (US)
                GoogleVoices.RACHEL,  // en-US-Chirp3-HD-Zephyr (US, diverse)
                GoogleVoices.MARY     // en-GB-Neural2-A (British)
        ));
    }

    public GoogleVoices getVoiceForCadence(ShipCadence cadence, GoogleVoices currentVoice) {
        List<GoogleVoices> voices = cadenceToVoices.getOrDefault(cadence, List.of());
        boolean isMale = currentVoice.isMale();
        Random random = new Random();

        List<GoogleVoices> matchingVoices = voices.stream()
                .filter(voice -> !voice.equals(currentVoice))
                .filter(voice -> voice.isMale() == isMale)
                .collect(Collectors.toList());

        return matchingVoices.isEmpty() ? currentVoice
                : matchingVoices.get(random.nextInt(matchingVoices.size()));
    }
}