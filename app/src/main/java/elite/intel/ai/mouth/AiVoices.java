package elite.intel.ai.mouth;

/**
 * If you implement another TTS, map these voices in your class to the
 * voices available in your TTS provider.
 *
 */
public enum AiVoices {
    //BETTY("Betty", 1.2, false),         // en-GB-Chirp3-HD-Aoede <-- change me!
    //CHARLES("Charles", 1.2, true),      // en-GB-Chirp3-HD-Algenib <-- change me!
    //KAREN("Karen", 1.2, false),         // en-GB-Neural2-A <-- change me!

    MARY("Mary", 1.3, false),           // en-US-Chirp3-HD-Zephyr
    ANNA("Anna", 1.1, false),           // en-GB-Chirp-HD-F
    EMMA("Emma", 1.2, false),           // en-US-Chirp3-HD-Despina
    JAKE("Jake", 1.2, false),           // en-US-Chirp3-HD-Iapetus
    JAMES("James", 1.2, true),          // en-AU-Chirp3-HD-Algieba
    JENNIFER("Jennifer", 1.4, false),   // en-US-Chirp3-HD-Sulafat
    JOSEPH("Joseph", 1.2, true),        // en-US-Chirp3-HD-Sadachbia
    MICHAEL("Michael", 1.2, true),      // en-US-Chirp3-HD-Charon
    OLIVIA("Olivia", 1.2, false),       // en-GB-Chirp3-HD-Aoede
    RACHEL("Rachel", 1.2, false),       // en-US-Chirp3-HD-Zephyr
    STEVE("Steve", 1.2, true),          // en-US-Chirp3-HD-Algenib
    ;

    // Default voice settings
    private String name = "James";
    private double speechRate = 1.2;
    private boolean isMale = true;


    AiVoices(String name, double speechRate, boolean gender) {
        this.name = name;
        this.speechRate = speechRate;
        this.isMale = gender;
    }

    public double getSpeechRate() {
        return speechRate;
    }

    public String getName() {
        return name;
    }

    public boolean isMale() {
        return isMale;
    }
}
