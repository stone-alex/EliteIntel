package elite.intel.ai.mouth.subscribers.events;

public class VocalisationRequestEvent extends BaseVoxEvent {
    private final Class<? extends BaseVoxEvent> originType;

    public VocalisationRequestEvent(String textToVoice, Class<? extends BaseVoxEvent> originType) {
        super(textToVoice, false);
        this.originType = originType;
    }

    public VocalisationRequestEvent(String textToVoice, boolean useRandom, Class<? extends BaseVoxEvent> originType) {
        super(textToVoice, useRandom);
        this.originType = originType;
    }

    public Class<? extends BaseVoxEvent> getOriginType() {
        return originType;
    }
}