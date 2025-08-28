package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;

public class RedeemVoucherEvent extends BaseEvent {
    @SerializedName("Type")
    private String type;

    @SerializedName("Amount")
    private long amount;

    @SerializedName("Factions")
    private List<Faction> factions;

    public RedeemVoucherEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), RedeemVoucherEvent.class.getName());
    }

    // Getters
    public String getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public List<Faction> getFactions() {
        return factions;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    public static class Faction {
        @SerializedName("Faction")
        private String faction;

        @SerializedName("Amount")
        private long amount;

        public String getFaction() {
            return faction;
        }

        public long getAmount() {
            return amount;
        }
    }

    @Override
    public String toString() {
        return new StringJoiner("Voucher redeemed: ")
                .add("type='" + type + "'")
                .add("amount=" + amount)
                .add("factions=" + factions)
                .toString();
    }
}