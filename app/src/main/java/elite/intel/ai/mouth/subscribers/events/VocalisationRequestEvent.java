package elite.intel.ai.mouth.subscribers.events;

import elite.intel.session.ChatHistory;
import elite.intel.session.SystemSession;

public class VocalisationRequestEvent extends BaseVoxEvent {

    private final Class<? extends BaseVoxEvent> originType;
    private boolean canBeInterrupted = true;

    public VocalisationRequestEvent(String textToVoice, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        super(textToVoice, false);
        SystemSession.getInstance().setChatHistory(new ChatHistory(null, textToVoice));
        this.originType = originType;
        this.canBeInterrupted = canBeInterrupted;
    }

    public VocalisationRequestEvent(String textToVoice, boolean useRandom, Class<? extends BaseVoxEvent> originType, boolean canBeInterrupted) {
        super(textToVoice, useRandom);
        this.originType = originType;
        this.canBeInterrupted = canBeInterrupted;
    }


    public Class<? extends BaseVoxEvent> getOriginType() {
        return originType;
    }

    public boolean canBeInterrupted() {
        return canBeInterrupted;
    }
}