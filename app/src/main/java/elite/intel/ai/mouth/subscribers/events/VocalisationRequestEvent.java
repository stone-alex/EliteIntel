package elite.intel.ai.mouth.subscribers.events;

public class VocalisationRequestEvent extends BaseVoxEvent {

    private final Class<? extends BaseVoxEvent> originType;
    private final String voiceName;
    private boolean canBeInterrupted;
    private final boolean isRadio;

    public VocalisationRequestEvent(String textToVoice, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        this(textToVoice, null, originType, canBeInterrupted, false);
    }

    public VocalisationRequestEvent(String textToVoice, String voiceName, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        this(textToVoice, voiceName, originType, canBeInterrupted, false);
    }

    public VocalisationRequestEvent(String textToVoice, String voiceName, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted, boolean isRadio) {
        super(textToVoice, false);
        this.voiceName = voiceName;
        this.originType = originType;
        this.canBeInterrupted = canBeInterrupted;
        this.isRadio = isRadio;
    }

    public Class<? extends BaseVoxEvent> getOriginType() {
        return originType;
    }

    public boolean canBeInterrupted() {
        return canBeInterrupted;
    }

    /**
     * Voice name (enum name). Null means use the session default.
     */
    public String getVoiceName() {
        return voiceName;
    }

    /**
     * True when this vocalisation should be processed through the radio transmission filter.
     */
    public boolean isRadio() {
        return isRadio;
    }
}