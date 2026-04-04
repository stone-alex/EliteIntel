package elite.intel.gameapi;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.BaseEvent;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class UserInputEvent extends BaseEvent {

    public UserInputEvent(String sanitizedTranscript) {
        super(Instant.now().toString(), Duration.ofSeconds(10), "UserInput");
        this.userInput = sanitizedTranscript;
    }

    private String userInput;

    public String getUserInput() {
        return userInput;
    }

    @Override public String getEventType() {
        return "UserInput";
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
