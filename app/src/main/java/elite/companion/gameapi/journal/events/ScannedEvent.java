package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class ScannedEvent extends BaseEvent {

    @SerializedName("ScanType")
    private String scanType;

    public ScannedEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), ScannedEvent.class.getName());
    }

    public String getScanType() {
        return scanType;
    }
}
