package elite.intel.gameapi;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.BaseEvent;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class VoiceProcessEvent extends BaseEvent {

    public VoiceProcessEvent(String textToVoice) {
        super(Instant.now().toString(), 1, Duration.ofSeconds(10), "VoiceProcess");
        this.textToVoice = textToVoice;
        this.useRandom = false;
    }

    public VoiceProcessEvent(String textToVoice, boolean useRandom) {
        super(Instant.now().toString(), 1, Duration.ofSeconds(10), "VoiceProcess");
        this.textToVoice = textToVoice;
        this.useRandom = useRandom;
    }


    private String textToVoice;
    private boolean useRandom;

    public String getText() {
        return textToVoice;
    }


    public boolean isUseRandom() {
        return useRandom;
    }

    @Override public String getEventType() {
        return "VoiceProcess";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

}
