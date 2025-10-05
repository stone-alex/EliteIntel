package elite.intel.ai.mouth.subscribers.events;

import elite.intel.gameapi.journal.events.BaseEvent;

import java.time.Duration;
import java.time.Instant;

public class BaseVoxEvent {
    private final String textToVoice;
    private final boolean useRandom;
    private final String timeStamp;


    public BaseVoxEvent(String textToVoice, boolean useRandom) {
        this.timeStamp = Instant.now().toString();
        this.textToVoice = textToVoice;
        this.useRandom = useRandom;
    }

    public String getText() {
        return textToVoice;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public boolean useRandomVoice() {
        return useRandom;
    }
}
