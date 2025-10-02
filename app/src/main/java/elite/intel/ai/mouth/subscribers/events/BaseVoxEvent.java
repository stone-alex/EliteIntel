package elite.intel.ai.mouth.subscribers.events;

public class BaseVoxEvent {
    private final String textToVoice;
    private final boolean useRandom;


    public BaseVoxEvent(String textToVoice, boolean useRandom) {
        this.textToVoice = textToVoice;
        this.useRandom = useRandom;
    }

    public String getText() {
        return textToVoice;
    }

    public boolean useRandomVoice() {
        return useRandom;
    }
}
