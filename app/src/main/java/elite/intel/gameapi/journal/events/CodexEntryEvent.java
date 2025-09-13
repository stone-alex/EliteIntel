package elite.intel.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.intel.util.TimestampFormatter;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;

public class CodexEntryEvent extends BaseEvent {
    @SerializedName("EntryID")
    private long entryID;

    @SerializedName("Name")
    private String name;

    @SerializedName("Name_Localised")
    private String nameLocalised;

    @SerializedName("SubCategory")
    private String subCategory;

    @SerializedName("SubCategory_Localised")
    private String subCategoryLocalised;

    @SerializedName("Category")
    private String category;

    @SerializedName("Category_Localised")
    private String categoryLocalised;

    @SerializedName("Region")
    private String region;

    @SerializedName("Region_Localised")
    private String regionLocalised;

    @SerializedName("System")
    private String system;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("BodyID")
    private int bodyID;

    @SerializedName("NearestDestination")
    private String nearestDestination;

    @SerializedName("Latitude")
    private double latitude;

    @SerializedName("Longitude")
    private double longitude;

    @SerializedName("IsNewEntry")
    private boolean isNewEntry;

    public CodexEntryEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "CodexEntry");
        CodexEntryEvent event = GsonFactory.getGson().fromJson(json, CodexEntryEvent.class);
        this.entryID = event.entryID;
        this.name = event.name;
        this.nameLocalised = event.nameLocalised;
        this.subCategory = event.subCategory;
        this.subCategoryLocalised = event.subCategoryLocalised;
        this.category = event.category;
        this.categoryLocalised = event.categoryLocalised;
        this.region = event.region;
        this.regionLocalised = event.regionLocalised;
        this.system = event.system;
        this.systemAddress = event.systemAddress;
        this.bodyID = event.bodyID;
        this.nearestDestination = event.nearestDestination;
        this.latitude = event.latitude;
        this.longitude = event.longitude;
        this.isNewEntry = event.isNewEntry;
    }

    @Override
    public String getEventType() {
        return "CodexEntry";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public long getEntryID() {
        return entryID;
    }

    public String getName() {
        return name;
    }

    public String getNameLocalised() {
        return nameLocalised;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getSubCategoryLocalised() {
        return subCategoryLocalised;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryLocalised() {
        return categoryLocalised;
    }

    public String getRegion() {
        return region;
    }

    public String getRegionLocalised() {
        return regionLocalised;
    }

    public String getSystem() {
        return system;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public int getBodyID() {
        return bodyID;
    }

    public String getNearestDestination() {
        return nearestDestination;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isNewEntry() {
        return isNewEntry;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}