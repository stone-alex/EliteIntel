package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;
import java.util.List;

public class MaterialsEvent extends BaseEvent {
    @SerializedName("Raw")
    private List<Material> raw;

    @SerializedName("Manufactured")
    private List<Material> manufactured;

    @SerializedName("Encoded")
    private List<Material> encoded;

    public MaterialsEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), MaterialsEvent.class.getName());
    }

    // Getters
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

        // Getters
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