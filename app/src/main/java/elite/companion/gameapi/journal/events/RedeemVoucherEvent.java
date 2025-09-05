package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
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

    public RedeemVoucherEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "RedeemVoucher");
        RedeemVoucherEvent event = GsonFactory.getGson().fromJson(json, RedeemVoucherEvent.class);
        this.type = event.type;
        this.amount = event.amount;
        this.factions = event.factions;
    }

    @Override
    public String getEventType() {
        return "RedeemVoucher";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

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