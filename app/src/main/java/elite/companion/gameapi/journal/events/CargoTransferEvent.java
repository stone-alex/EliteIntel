package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;

public class CargoTransferEvent extends BaseEvent {
    @SerializedName("Transfers")
    private List<Transfer> transfers;

    public CargoTransferEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), CargoTransferEvent.class.getName());
    }

    // Getter
    public List<Transfer> getTransfers() {
        return transfers;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    public static class Transfer {
        @SerializedName("Type")
        private String type;

        @SerializedName("Type_Localised")
        private String typeLocalised;

        @SerializedName("Count")
        private int count;

        @SerializedName("Direction")
        private String direction;

        public String getType() {
            return type;
        }

        public String getTypeLocalised() {
            return typeLocalised;
        }

        public int getCount() {
            return count;
        }

        public String getDirection() {
            return direction;
        }
    }

    @Override
    public String toString() {
        return new StringJoiner("Cargo transfer detected: ")
                .add("transfers=" + transfers)
                .toString();
    }
}