package elite.companion.gameapi.journal.events.dto;

import elite.companion.util.GsonFactory;
import elite.companion.util.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;

public class StellarObjectDto  implements ToJsonConvertible {


    private String name;
    private double surfaceGravity;
    private double surfaceTemperature;
    private boolean isLandable;
    private String planetClass;
    private boolean isTerraformable;
    List<MaterialDto> materials = new ArrayList<>();
    private boolean isTidalLocked;
    private String atmosphere;

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    public void setName(String bodyName) {
        this.name = bodyName;
    }

    public void setGravity(double surfaceGravity) {
        this.surfaceGravity = surfaceGravity;
    }

    public void setSurfaceTemperature(double surfaceTemperature) {
        this.surfaceTemperature = surfaceTemperature;
    }

    public void setLandable(boolean landable) {
        this.isLandable = landable;
    }

    public void setPlanetClass(String planetClass) {
        this.planetClass = planetClass;
    }

    public void setIsTerraformable(boolean isTerraformable) {
        this.isTerraformable = isTerraformable;

    }

    public void setMaterials(List<MaterialDto> materials) {
        this.materials.addAll(materials);
    }

    public String getName() {
        return name;
    }

    public double getSurfaceGravity() {
        return surfaceGravity;
    }

    public double getSurfaceTemperature() {
        return surfaceTemperature;
    }

    public boolean isLandable() {
        return isLandable;
    }

    public String getPlanetClass() {
        return planetClass;
    }

    public boolean isTerraformable() {
        return isTerraformable;
    }

    public List<MaterialDto> getMaterials() {
        return materials;
    }

    public void setTidalLocked(boolean tidalLock) {
        this.isTidalLocked = tidalLock;
    }

    public boolean isTidalLocked() {
        return isTidalLocked;
    }

    public void setAtmosphere(String atmosphereType) {
        this.atmosphere = atmosphereType;
    }

    public String getAtmosphere() {
        return atmosphere;
    }
}

