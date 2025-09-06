package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.json.GsonFactory;

import java.time.Duration;
import java.util.List;

public class SwitchSuitLoadoutEvent extends BaseEvent {
    @SerializedName("SuitID")
    private long suitID;

    @SerializedName("SuitName")
    private String suitName;

    @SerializedName("SuitName_Localised")
    private String suitNameLocalised;

    @SerializedName("SuitMods")
    private List<String> suitMods;

    @SerializedName("LoadoutID")
    private long loadoutID;

    @SerializedName("LoadoutName")
    private String loadoutName;

    @SerializedName("Modules")
    private List<Module> modules;

    public SwitchSuitLoadoutEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "SwitchSuitLoadout");
        SwitchSuitLoadoutEvent event = GsonFactory.getGson().fromJson(json, SwitchSuitLoadoutEvent.class);
        this.suitID = event.suitID;
        this.suitName = event.suitName;
        this.suitNameLocalised = event.suitNameLocalised;
        this.suitMods = event.suitMods;
        this.loadoutID = event.loadoutID;
        this.loadoutName = event.loadoutName;
        this.modules = event.modules;
    }

    @Override
    public String getEventType() {
        return "SwitchSuitLoadout";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public long getSuitID() {
        return suitID;
    }

    public String getSuitName() {
        return suitName;
    }

    public String getSuitNameLocalised() {
        return suitNameLocalised;
    }

    public List<String> getSuitMods() {
        return suitMods;
    }

    public long getLoadoutID() {
        return loadoutID;
    }

    public String getLoadoutName() {
        return loadoutName;
    }

    public List<Module> getModules() {
        return modules;
    }

    public static class Module {
        @SerializedName("SlotName")
        private String slotName;

        @SerializedName("SuitModuleID")
        private long suitModuleID;

        @SerializedName("ModuleName")
        private String moduleName;

        @SerializedName("ModuleName_Localised")
        private String moduleNameLocalised;

        @SerializedName("Class")
        private int moduleClass;

        @SerializedName("WeaponMods")
        private List<String> weaponMods;

        public String getSlotName() {
            return slotName;
        }

        public long getSuitModuleID() {
            return suitModuleID;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getModuleNameLocalised() {
            return moduleNameLocalised;
        }

        public int getModuleClass() {
            return moduleClass;
        }

        public List<String> getWeaponMods() {
            return weaponMods;
        }
    }
}