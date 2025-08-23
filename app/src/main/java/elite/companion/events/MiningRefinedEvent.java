package elite.companion.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.Instant;
import java.util.StringJoiner;

public class MiningRefinedEvent extends BaseEvent {

    @SerializedName("Type")
    private String type;

    @SerializedName("Type_Localised")
    private String typeLocalised;

    public MiningRefinedEvent(String timestamp, String type, String typeLocalised) {
        super(timestamp, 1, Duration.ZERO, MiningRefinedEvent.class.getName());
        this.type = type;
        this.typeLocalised = typeLocalised;
    }

    public String getType() {
        return type;
    }

    public String getTypeLocalised() {
        return typeLocalised;
    }

    @Override public String toString() {
        return new StringJoiner(", ", MiningRefinedEvent.class.getSimpleName() + "[", "]")
                .add("type='" + type + "'")
                .add("typeLocalised='" + typeLocalised + "'")
                .add("timestamp='" + timestamp + "'")
                .add("eventName='" + eventName + "'")
                .add("priority=" + priority)
                .add("endOfLife=" + endOfLife)
                .add("isProcessed=" + isProcessed)
                .toString();
    }
}
