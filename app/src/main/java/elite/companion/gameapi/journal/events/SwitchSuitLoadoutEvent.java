package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;

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

    public SwitchSuitLoadoutEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), SwitchSuitLoadoutEvent.class.getName());
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
}