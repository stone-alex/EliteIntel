package elite.companion.gameapi;

import com.google.gson.JsonObject;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.util.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class UserInputEvent extends BaseEvent {

    public UserInputEvent(String sanitizedTranscript, float confidence) {
        super(Instant.now().toString(), 1, Duration.ofSeconds(10), "UserInput");
        this.userInput = sanitizedTranscript;
        this.confidence = confidence;
    }

    private String userInput;
    private float confidence;


    public String getUserInput() {
        return userInput;
    }

    public float getConfidence() {
        return confidence;
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
