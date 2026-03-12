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

    AF_HEART(0, false, "Heart", "American female — warm, natural (default)"),
    AF_BELLA(1, false, "Bella", "American female — expressive"),
    AF_NICOLE(2, false, "Nicole", "American female — clear"),
    AF_SKY(3, false, "Sky", "American female — bright"),
    AM_ADAM(4, true, "Adam", "American male — deep"),
    AM_MICHAEL(5, true, "Michael", "American male — authoritative"),
    BF_EMMA(6, false, "Emma", "British female — precise"),
    BF_ISABELLA(7, false, "Isabella", "British female — refined"),
    BM_GEORGE(8, true, "George", "British male — commanding"),
    BM_LEWIS(9, true, "Lewis", "British male — measured"),
    BM_DANIEL(10, true, "Daniel", "British male — calm");

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