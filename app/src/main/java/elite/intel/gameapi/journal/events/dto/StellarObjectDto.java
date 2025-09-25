package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;

public class StellarObjectDto implements ToJsonConvertible {

    private String name;
    private long bodyId;
    private String shortName;
    private double surfaceGravity;
    private double surfaceTemperature;
    private boolean isLandable;
    private String planetClass;
    private boolean isTerraformable;
    List<MaterialDto> materials = new ArrayList<>();
    private boolean isTidalLocked;
    private String atmosphere;
    private double radius;
    private double massEM;
    private int numberOfBioFormsPresent;
    private int geoSignals;
    private boolean hasRings;

    List<SAASignalsFoundEvent.Genus> genus = new ArrayList<>();
    List<SAASignalsFoundEvent.Signal> saasSignals = new ArrayList<>();
    private List<FSSBodySignalsEvent.Signal> fssSignals;

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }


    public long getBodyId() {
        return bodyId;
    }

    public void setBodyId(long bodyId) {
        this.bodyId = bodyId;
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

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getMassEM() {
        return massEM;
    }

    public void setMassEM(double massEM) {
        this.massEM = massEM;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getNumberOfBioFormsPresent() {
        return numberOfBioFormsPresent;
    }
    public void setNumberOfBioFormsPresent(int numberOfBioFormsPresent) {
        this.numberOfBioFormsPresent = numberOfBioFormsPresent;
    }

    public List<SAASignalsFoundEvent.Genus> getGenus() {
        return genus;
    }
    public void setGenus(List<SAASignalsFoundEvent.Genus> genus) {
        this.genus.addAll(genus);
    }

    public int getGeoSignals() {
        return geoSignals;
    }

    public void setGeoSignals(int geoSignals) {
        this.geoSignals = geoSignals;
    }

    public List<SAASignalsFoundEvent.Signal> getSaasSignals() {
        return saasSignals;
    }

    public void setSaasSignals(List<SAASignalsFoundEvent.Signal> saasSignals) {
        this.saasSignals = saasSignals;
    }

    public void setSurfaceGravity(double surfaceGravity) {
        this.surfaceGravity = surfaceGravity;
    }

    public void setTerraformable(boolean terraformable) {
        isTerraformable = terraformable;
    }

    public boolean isHasRings() {
        return hasRings;
    }

    public void setHasRings(boolean hasRings) {
        this.hasRings = hasRings;
    }

    public void setFssSignals(List<FSSBodySignalsEvent.Signal> signals) {
        this.fssSignals = signals;
    }

    public List<FSSBodySignalsEvent.Signal> getFssSignals() {
        return fssSignals;
    }
}

