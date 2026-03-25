package elite.intel.ai.mouth.subscribers.events;

import elite.intel.session.ChatHistory;
import elite.intel.session.SystemSession;

public class VocalisationRequestEvent extends BaseVoxEvent {

    private final Class<? extends BaseVoxEvent> originType;
    private final String voiceName;
    private boolean canBeInterrupted = true;

    public VocalisationRequestEvent(String textToVoice, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        this(textToVoice, null, originType, canBeInterrupted);
        SystemSession.getInstance().setChatHistory(new ChatHistory(null, textToVoice));
    }

    public VocalisationRequestEvent(String textToVoice, boolean useRandom, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        super(textToVoice, useRandom);
        this.voiceName = null;
        this.originType = originType;
        this.canBeInterrupted = canBeInterrupted;
    }

    public VocalisationRequestEvent(String textToVoice, String voiceName, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        super(textToVoice, false);
        this.voiceName = voiceName;
        this.originType = originType;
        this.canBeInterrupted = canBeInterrupted;
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
}