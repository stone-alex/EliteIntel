package elite.intel.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;

public class ScanEvent extends BaseEvent {
    @SerializedName("ScanType")
    private String scanType;

    @SerializedName("BodyName")
    private String bodyName;

    @SerializedName("BodyID")
    private int bodyID;

    @SerializedName("Parents")
    private List<Parent> parents;

    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("StarType")
    private String starType;

    @SerializedName("SystemAddress")
    private long systemAddress;

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
    private List<Material> materials;

    @SerializedName("Composition")
    private Composition composition;

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

    // Nested class for Parents
    public static class Parent {
        @SerializedName("Planet")
        private Integer planet;

        @SerializedName("Star")
        private Integer star;

        public Integer getPlanet() {
            return planet;
        }

        public Integer getStar() {
            return star;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Parent.class.getSimpleName() + "[", "]")
                    .add("planet=" + planet)
                    .add("star=" + star)
                    .toString();
        }
    }

    // Nested class for Materials
    public static class Material {
        @SerializedName("Name")
        private String name;

        @SerializedName("Percent")
        private double percent;

        public String getName() {
            return name;
        }

        public double getPercent() {
            return percent;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Material.class.getSimpleName() + "[", "]")
                    .add("name='" + name + "'")
                    .add("percent=" + percent)
                    .toString();
        }
    }

    // Nested class for Composition
    public static class Composition {
        @SerializedName("Ice")
        private double ice;

        @SerializedName("Rock")
        private double rock;

        @SerializedName("Metal")
        private double metal;

        public double getIce() {
            return ice;
        }

        public double getRock() {
            return rock;
        }

        public double getMetal() {
            return metal;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Composition.class.getSimpleName() + "[", "]")
                    .add("ice=" + ice)
                    .add("rock=" + rock)
                    .add("metal=" + metal)
                    .toString();
        }
    }

    public ScanEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), Duration.ofSeconds(15), "Scan");
        ScanEvent event = GsonFactory.getGson().fromJson(json, ScanEvent.class);
        this.scanType = event.scanType;
        this.bodyName = event.bodyName;
        this.bodyID = event.bodyID;
        this.parents = event.parents;
        this.starSystem = event.starSystem;
        this.systemAddress = event.systemAddress;
        this.distanceFromArrivalLS = event.distanceFromArrivalLS;
        this.tidalLock = event.tidalLock;
        this.terraformState = event.terraformState;
        this.planetClass = event.planetClass;
        this.atmosphere = event.atmosphere;
        this.atmosphereType = event.atmosphereType;
        this.volcanism = event.volcanism;
        this.massEM = event.massEM;
        this.radius = event.radius;
        this.surfaceGravity = event.surfaceGravity;
        this.surfaceTemperature = event.surfaceTemperature;
        this.surfacePressure = event.surfacePressure;
        this.landable = event.landable;
        this.materials = event.materials;
        this.composition = event.composition;
        this.semiMajorAxis = event.semiMajorAxis;
        this.eccentricity = event.eccentricity;
        this.orbitalInclination = event.orbitalInclination;
        this.periapsis = event.periapsis;
        this.orbitalPeriod = event.orbitalPeriod;
        this.ascendingNode = event.ascendingNode;
        this.meanAnomaly = event.meanAnomaly;
        this.rotationPeriod = event.rotationPeriod;
        this.axialTilt = event.axialTilt;
        this.wasDiscovered = event.wasDiscovered;
        this.wasMapped = event.wasMapped;
    }

    @Override
    public String getEventType() {
        return "Scan";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    // Getters
    public String getScanType() {
        return scanType;
    }

    public String getBodyName() {
        return bodyName;
    }

    public int getBodyID() {
        return bodyID;
    }

    public List<Parent> getParents() {
        return parents;
    }

    public String getStarSystem() {
        return starSystem;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public double getDistanceFromArrivalLS() {
        return distanceFromArrivalLS;
    }

    public boolean isTidalLock() {
        return tidalLock;
    }

    public String getTerraformState() {
        return terraformState;
    }

    public String getPlanetClass() {
        return planetClass;
    }

    public String getAtmosphere() {
        return atmosphere;
    }

    public String getAtmosphereType() {
        return atmosphereType;
    }

    public String getVolcanism() {
        return volcanism;
    }

    public double getMassEM() {
        return massEM;
    }

    public double getRadius() {
        return radius;
    }

    public double getSurfaceGravity() {
        return surfaceGravity;
    }

    public double getSurfaceTemperature() {
        return surfaceTemperature;
    }

    public double getSurfacePressure() {
        return surfacePressure;
    }

    public boolean isLandable() {
        return landable;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public Composition getComposition() {
        return composition;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getOrbitalInclination() {
        return orbitalInclination;
    }

    public double getPeriapsis() {
        return periapsis;
    }

    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public double getAscendingNode() {
        return ascendingNode;
    }

    public double getMeanAnomaly() {
        return meanAnomaly;
    }

    public double getRotationPeriod() {
        return rotationPeriod;
    }

    public double getAxialTilt() {
        return axialTilt;
    }

    public boolean isWasDiscovered() {
        return wasDiscovered;
    }

    public boolean isWasMapped() {
        return wasMapped;
    }


    public String getStarType() {
        return starType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ScanEvent.class.getSimpleName() + "[", "]")
                .add("scanType='" + scanType + "'")
                .add("bodyName='" + bodyName + "'")
                .add("bodyID=" + bodyID)
                .add("parents=" + parents)
                .add("starSystem='" + starSystem + "'")
                .add("systemAddress=" + systemAddress)
                .add("distanceFromArrivalLS=" + distanceFromArrivalLS)
                .add("tidalLock=" + tidalLock)
                .add("terraformState='" + terraformState + "'")
                .add("planetClass='" + planetClass + "'")
                .add("atmosphere='" + atmosphere + "'")
                .add("atmosphereType='" + atmosphereType + "'")
                .add("volcanism='" + volcanism + "'")
                .add("massEM=" + massEM)
                .add("radius=" + radius)
                .add("surfaceGravity=" + surfaceGravity)
                .add("surfaceTemperature=" + surfaceTemperature)
                .add("surfacePressure=" + surfacePressure)
                .add("landable=" + landable)
                .add("materials=" + materials)
                .add("composition=" + composition)
                .add("semiMajorAxis=" + semiMajorAxis)
                .add("eccentricity=" + eccentricity)
                .add("orbitalInclination=" + orbitalInclination)
                .add("periapsis=" + periapsis)
                .add("orbitalPeriod=" + orbitalPeriod)
                .add("ascendingNode=" + ascendingNode)
                .add("meanAnomaly=" + meanAnomaly)
                .add("rotationPeriod=" + rotationPeriod)
                .add("axialTilt=" + axialTilt)
                .add("wasDiscovered=" + wasDiscovered)
                .add("wasMapped=" + wasMapped)
                .add("timestamp='" + timestamp + "'")
                .add("eventName='" + eventName + "'")
                .add("endOfLife=" + endOfLife)
                .toString();
    }
}