package elite.intel.ai.mouth.kokoro;

/**
 * Speaker IDs for the kokoro-en-v0_19 model.
 * <p>
 * Confirmed speaker list from:
 * https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/kokoro.html
 * <p>
 * af_ = American Female, am_ = American Male
 * bf_ = British Female,  bm_ = British Male
 */
public enum KokoroVoices {

    HEART(0, false, "Heart", "American female (default)"), //F
    ANNA(4, true, "Anna", "American female"), //F
    ISABELLA(7, false, "Isabella", "British female"), //F
    EMMA(8, true, "Emma", "British male"), //F
    NICOLE(2, false, "Nicole", "American female - (whispering)"), //F
    SKY(3, false, "Sky", "American female"), //F
    BELLA(1, false, "Bella", "American female"), //F

    GEORGE(6, false, "Emma", "British female — precise"),
    MICHAEL(5, true, "Michael", "American male — authoritative"),
    JASON(9, true, "Jason", "British male — measured"),
    DANIEL(10, true, "Daniel", "British male — calm");

    private final int sid;
    private final boolean male;
    private final String displayName;
    private final String description;

    KokoroVoices(int sid, boolean male, String displayName, String description) {
        this.sid = sid;
        this.male = male;
        this.displayName = displayName;
        this.description = description;
    }

    public int getSid() {
        return sid;
    }

    public boolean isMale() {
        return male;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description; }
}