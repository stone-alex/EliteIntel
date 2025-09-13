package elite.intel.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.intel.util.TimestampFormatter;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;
import java.util.List;

public class MaterialsEvent extends BaseEvent {
    @SerializedName("Raw")
    private List<Material> raw;

    @SerializedName("Manufactured")
    private List<Material> manufactured;

    @SerializedName("Encoded")
    private List<Material> encoded;

    public MaterialsEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofDays(30), "Materials");
        MaterialsEvent event = GsonFactory.getGson().fromJson(json, MaterialsEvent.class);
        this.raw = event.raw;
        this.manufactured = event.manufactured;
        this.encoded = event.encoded;
    }

    @Override
    public String getEventType() {
        return "Materials";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public List<Material> getRaw() {
        return raw;
    }

    public List<Material> getManufactured() {
        return manufactured;
    }

    public List<Material> getEncoded() {
        return encoded;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    public static class Material {
        @SerializedName("Name")
        private String name;

        @SerializedName("Name_Localised")
        private String localisedName;

        @SerializedName("Count")
        private int count;

        public String getName() {
            return name;
        }

        public String getLocalisedName() {
            return localisedName;
        }

        public int getCount() {
            return count;
        }
    }
}