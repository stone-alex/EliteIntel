package elite.intel.ai.mouth.subscribers.events;

public class VocalisationRequestEvent extends BaseVoxEvent {
    private final Class<? extends BaseVoxEvent> originType;
    private boolean isChatStreamChatVolcaisation;
    private boolean canBeInterrupted = true;

    public VocalisationRequestEvent(String textToVoice, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        super(textToVoice, false);
        this.originType = originType;
        this.canBeInterrupted = canBeInterrupted;
    }

    public VocalisationRequestEvent(String textToVoice, boolean useRandom, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        super(textToVoice, useRandom);
        this.originType = originType;
        this.canBeInterrupted = canBeInterrupted;
    }

    public VocalisationRequestEvent(String textToVoice, boolean useRandom, boolean isChatStreamChatVolcaisation, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        super(textToVoice, useRandom);
        this.originType = originType;
        this.isChatStreamChatVolcaisation = isChatStreamChatVolcaisation;
        this.canBeInterrupted = canBeInterrupted;
    }


    public Class<? extends BaseVoxEvent> getOriginType() {
        return originType;
    }

    public boolean isChatStreamChatVolcaisation() {
        return isChatStreamChatVolcaisation;
    }

    public boolean canBeInterrupted() {
        return canBeInterrupted;
    }
}