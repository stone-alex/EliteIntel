package elite.intel.gameapi.journal.events.dto.shiploadout;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class ModuleDto extends BaseJsonDto implements ToJsonConvertible {

    private String slot;
    private String item;
    private boolean isOn;
    private int priority;
    private float healthPercentage;
    private Long value;
    private Integer ammoInClip;
    private Integer ammoInHopper;
    private EngineeringDto engineering;

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public float getHealthPercentage() {
        return healthPercentage;
    }

    public void setHealthPercentage(float healthPercentage) {
        this.healthPercentage = healthPercentage;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Integer getAmmoInClip() {
        return ammoInClip;
    }

    public void setAmmoInClip(Integer ammoInClip) {
        this.ammoInClip = ammoInClip;
    }

    public Integer getAmmoInHopper() {
        return ammoInHopper;
    }

    public void setAmmoInHopper(Integer ammoInHopper) {
        this.ammoInHopper = ammoInHopper;
    }

    public EngineeringDto getEngineering() {
        return engineering;
    }

    public void setEngineering(EngineeringDto engineering) {
        this.engineering = engineering;
    }
}
