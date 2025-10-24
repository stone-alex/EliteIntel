package elite.intel.ai;

public class TtsEvent {

    private boolean isSpeaking;

    public TtsEvent(boolean isSpeaking) {
        this.isSpeaking = isSpeaking;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }
}
