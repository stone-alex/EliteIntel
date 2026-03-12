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

    HEART(0, false, "Heart", "American female"),
    BELLA(1, false, "Bella", "American female"),
    NICOLE(2, false, "Nicole", "American female - whispering"),
    SKY(3, false, "Sky", "American female"),
    ANNA(4, false, "Anna", "American female"),
    MICHAEL(5, true, "Michael", "American male"),
    GEORGE(6, true, "George", "British male"),
    ISABELLA(7, false, "Isabella", "British female"),
    EMMA(8, false, "Emma", "British female"),
    JASON(9, true, "Jason", "British male"),
    DANIEL(10, true, "Daniel", "British male");

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