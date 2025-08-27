package elite.companion.events;

import com.google.gson.annotations.SerializedName;
import java.time.Duration;

public class ReceiveTextEvent extends BaseEvent {

    @SerializedName("From")
    public String from;

    @SerializedName("From_Localised")
    public String fromLocalised;

    @SerializedName("Message")
    public String message;

    @SerializedName("Message_Localised")
    public String messageLocalised;

    @SerializedName("Channel")
    public String channel;

    public ReceiveTextEvent(String timestamp, String from, String message, String messageLocalised, String channel) {
        super(timestamp, 2, Duration.ofMinutes(1), ReceiveTextEvent.class.getName());
        this.from = from;
        this.message = message;
        this.messageLocalised = messageLocalised;
        this.channel = channel;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageLocalised() {
        return messageLocalised;
    }

    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return String.format("%s: Received text on channel %s: %s", timestamp, channel, messageLocalised);
    }

    public String getFromLocalised() {
        return fromLocalised;
    }
}