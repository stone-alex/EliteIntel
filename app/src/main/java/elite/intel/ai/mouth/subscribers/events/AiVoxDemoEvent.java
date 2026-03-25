package elite.intel.ai.mouth.subscribers.events;

public class AiVoxDemoEvent extends BaseVoxEvent {

    private final String voiceName;

    public AiVoxDemoEvent(String textToVoice, String voiceName) {
        super(textToVoice, false);
        this.voiceName = voiceName;
    }

    public String getVoiceName() {
        return voiceName;
    }
}
