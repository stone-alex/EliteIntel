package elite.companion.ai.mouth;

public enum GoogleVoices {
    ANNA("Anna", 1.1, false),           // en-GB-Chirp-HD-F
    BETTY("Betty", 1.2, false),         // en-GB-Chirp3-HD-Aoede
    CHARLES("Charles", 1.2, true),      // en-GB-Chirp3-HD-Algenib
    EMMA("Emma", 1.1, false),           // en-US-Chirp3-HD-Despina
    JAKE("Jake", 1.2, false),           // en-US-Chirp3-HD-Iapetus
    JAMES("James", 1.1, true),          // en-AU-Chirp3-HD-Algieba
    JENNIFER("Jennifer", 1.2, false),   // en-US-Chirp3-HD-Sulafat
    JOSEPH("Joseph", 1.2, true),        // en-US-Chirp3-HD-Sadachbia
    KAREN("Karen", 1.2, false),         // en-GB-Chirp3-HD-Zephyr
    MARY("Mary", 1.2, false),           // en-GB-Neural2-A
    MICHAEL("Michael", 1.2, true),      // en-US-Chirp3-HD-Charon
    OLIVIA("Olivia", 1.2, false),       // en-GB-Chirp3-HD-Aoede
    RACHEL("Rachel", 1.2, false),       // en-US-Chirp3-HD-Zephyr
    STEVE("Steve", 1.2, true),          // en-US-Chirp3-HD-Algenib
    ;

    // Default voice settings
    private String name = "James";
    private double speechRate = 1.2;
    private boolean isMale = true;


    GoogleVoices(String name, double speechRate, boolean gender) {
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
