package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.journal.events.ScanEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class MaterialDto extends ScanEvent.Material implements ToJsonConvertible {

    private String materialName;
    private double materialPercentage;

    public MaterialDto(String name, double percent) {
        this.materialName = name;
        this.materialPercentage = percent;
    }


    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public double getMaterialPercentage() {
        return materialPercentage;
    }

    public void setMaterialPercentage(double materialPercentage) {
        this.materialPercentage = materialPercentage;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
