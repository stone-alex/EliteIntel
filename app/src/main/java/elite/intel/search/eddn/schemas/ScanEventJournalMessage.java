package elite.intel.search.eddn.schemas;

import com.google.gson.annotations.SerializedName;
import elite.intel.gameapi.journal.events.ScanEvent;

import java.util.List;

public class ScanEventJournalMessage {

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("event")
    private String event = "Scan";

    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("StarPos")
    private List<Double> starPos; // [x, y, z] - required, add to session if missing

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("horizons")
    private Boolean horizons; // optional, from Fileheader/LoadGame

    @SerializedName("odyssey")
    private Boolean odyssey; // optional, from Fileheader/LoadGame

    // All other Scan fields (copy directly)
    @SerializedName("ScanType")
    private String scanType;

    @SerializedName("BodyName")
    private String bodyName;

    @SerializedName("BodyID")
    private Long bodyID;

    @SerializedName("Parents")
    private List<ScanEvent.Parent> parents;

    @SerializedName("DistanceFromArrivalLS")
    private double distanceFromArrivalLS;

    @SerializedName("TidalLock")
    private boolean tidalLock;

    @SerializedName("TerraformState")
    private String terraformState;

    @SerializedName("PlanetClass")
    private String planetClass;

    @SerializedName("Atmosphere")
    private String atmosphere;

    @SerializedName("AtmosphereType")
    private String atmosphereType;

    @SerializedName("Volcanism")
    private String volcanism;

    @SerializedName("MassEM")
    private double massEM;

    @SerializedName("Radius")
    private double radius;

    @SerializedName("SurfaceGravity")
    private double surfaceGravity;

    @SerializedName("SurfaceTemperature")
    private double surfaceTemperature;

    @SerializedName("SurfacePressure")
    private double surfacePressure;

    @SerializedName("Landable")
    private boolean landable;

    @SerializedName("Materials")
    private List<ScanEvent.Material> materials;

    @SerializedName("Composition")
    private ScanEvent.Composition composition;

    @SerializedName("SemiMajorAxis")
    private double semiMajorAxis;

    @SerializedName("Eccentricity")
    private double eccentricity;

    @SerializedName("OrbitalInclination")
    private double orbitalInclination;

    @SerializedName("Periapsis")
    private double periapsis;

    @SerializedName("OrbitalPeriod")
    private double orbitalPeriod;

    @SerializedName("AscendingNode")
    private double ascendingNode;

    @SerializedName("MeanAnomaly")
    private double meanAnomaly;

    @SerializedName("RotationPeriod")
    private double rotationPeriod;

    @SerializedName("AxialTilt")
    private double axialTilt;

    @SerializedName("WasDiscovered")
    private boolean wasDiscovered;

    @SerializedName("WasMapped")
    private boolean wasMapped;

    @SerializedName("StarType")
    private String starType;

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setStarSystem(String starSystem) {
        this.starSystem = starSystem;
    }

    public void setStarPos(List<Double> starPos) {
        this.starPos = starPos;
    }

    public void setSystemAddress(long systemAddress) {
        this.systemAddress = systemAddress;
    }

    public void setHorizons(Boolean horizons) {
        this.horizons = horizons;
    }

    public void setOdyssey(Boolean odyssey) {
        this.odyssey = odyssey;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public void setBodyName(String bodyName) {
        this.bodyName = bodyName;
    }

    public void setBodyID(Long bodyID) {
        this.bodyID = bodyID;
    }

    public void setParents(List<ScanEvent.Parent> parents) {
        this.parents = parents;
    }

    public void setDistanceFromArrivalLS(double distanceFromArrivalLS) {
        this.distanceFromArrivalLS = distanceFromArrivalLS;
    }

    public void setTidalLock(boolean tidalLock) {
        this.tidalLock = tidalLock;
    }

    public void setTerraformState(String terraformState) {
        this.terraformState = terraformState;
    }

    public void setPlanetClass(String planetClass) {
        this.planetClass = planetClass;
    }

    public void setAtmosphere(String atmosphere) {
        this.atmosphere = atmosphere;
    }

    public void setAtmosphereType(String atmosphereType) {
        this.atmosphereType = atmosphereType;
    }

    public void setVolcanism(String volcanism) {
        this.volcanism = volcanism;
    }

    public void setMassEM(double massEM) {
        this.massEM = massEM;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setSurfaceGravity(double surfaceGravity) {
        this.surfaceGravity = surfaceGravity;
    }

    public void setSurfaceTemperature(double surfaceTemperature) {
        this.surfaceTemperature = surfaceTemperature;
    }

    public void setSurfacePressure(double surfacePressure) {
        this.surfacePressure = surfacePressure;
    }

    public void setLandable(boolean landable) {
        this.landable = landable;
    }

    public void setMaterials(List<ScanEvent.Material> materials) {
        this.materials = materials;
    }

    public void setComposition(ScanEvent.Composition composition) {
        this.composition = composition;
    }

    public void setSemiMajorAxis(double semiMajorAxis) {
        this.semiMajorAxis = semiMajorAxis;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public void setOrbitalInclination(double orbitalInclination) {
        this.orbitalInclination = orbitalInclination;
    }

    public void setPeriapsis(double periapsis) {
        this.periapsis = periapsis;
    }

    public void setOrbitalPeriod(double orbitalPeriod) {
        this.orbitalPeriod = orbitalPeriod;
    }

    public void setAscendingNode(double ascendingNode) {
        this.ascendingNode = ascendingNode;
    }

    public void setMeanAnomaly(double meanAnomaly) {
        this.meanAnomaly = meanAnomaly;
    }

    public void setRotationPeriod(double rotationPeriod) {
        this.rotationPeriod = rotationPeriod;
    }

    public void setAxialTilt(double axialTilt) {
        this.axialTilt = axialTilt;
    }

    public void setWasDiscovered(boolean wasDiscovered) {
        this.wasDiscovered = wasDiscovered;
    }

    public void setWasMapped(boolean wasMapped) {
        this.wasMapped = wasMapped;
    }

    public void setStarType(String starType) {
        this.starType = starType;
    }
}