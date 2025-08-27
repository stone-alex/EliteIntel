package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;

public class ScanEvent extends BaseEvent {
    @SerializedName("ScanType")
    private String scanType;

    @SerializedName("BodyName")
    private String bodyName;

    @SerializedName("BodyID")
    private int bodyId;

    @SerializedName("Parents")
    private List<Parent> parents;

    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("DistanceFromArrivalLS")
    private float distanceFromArrivalLS;

    @SerializedName("StarType")
    private String starType;

    @SerializedName("Subclass")
    private int subclass;

    @SerializedName("StellarMass")
    private float stellarMass;

    @SerializedName("Radius")
    private double radius;

    @SerializedName("AbsoluteMagnitude")
    private float absoluteMagnitude;

    @SerializedName("Age_MY")
    private int ageMY;

    @SerializedName("SurfaceTemperature")
    private float surfaceTemperature;

    @SerializedName("Luminosity")
    private String luminosity;

    @SerializedName("SemiMajorAxis")
    private double semiMajorAxis;

    @SerializedName("Eccentricity")
    private float eccentricity;

    @SerializedName("OrbitalInclination")
    private float orbitalInclination;

    @SerializedName("Periapsis")
    private float periapsis;

    @SerializedName("OrbitalPeriod")
    private double orbitalPeriod;

    @SerializedName("AscendingNode")
    private float ascendingNode;

    @SerializedName("MeanAnomaly")
    private float meanAnomaly;

    @SerializedName("RotationPeriod")
    private float rotationPeriod;

    @SerializedName("AxialTilt")
    private float axialTilt;

    @SerializedName("WasDiscovered")
    private boolean wasDiscovered;

    @SerializedName("WasMapped")
    private boolean wasMapped;

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

    @SerializedName("AtmosphereComposition")
    private List<AtmosphereComposition> atmosphereComposition;

    @SerializedName("Volcanism")
    private String volcanism;

    @SerializedName("MassEM")
    private float massEM;

    @SerializedName("SurfaceGravity")
    private float surfaceGravity;

    @SerializedName("SurfacePressure")
    private float surfacePressure;

    @SerializedName("Landable")
    private boolean landable;

    @SerializedName("Materials")
    private List<Material> materials;

    @SerializedName("Composition")
    private Composition composition;

    public ScanEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), ScanEvent.class.getName());
    }

    // Getters
    public String getScanType() {
        return scanType;
    }

    public String getBodyName() {
        return bodyName;
    }

    public int getBodyId() {
        return bodyId;
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

    public float getDistanceFromArrivalLS() {
        return distanceFromArrivalLS;
    }

    public String getStarType() {
        return starType;
    }

    public int getSubclass() {
        return subclass;
    }

    public float getStellarMass() {
        return stellarMass;
    }

    public double getRadius() {
        return radius;
    }

    public float getAbsoluteMagnitude() {
        return absoluteMagnitude;
    }

    public int getAgeMY() {
        return ageMY;
    }

    public float getSurfaceTemperature() {
        return surfaceTemperature;
    }

    public String getLuminosity() {
        return luminosity;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public float getEccentricity() {
        return eccentricity;
    }

    public float getOrbitalInclination() {
        return orbitalInclination;
    }

    public float getPeriapsis() {
        return periapsis;
    }

    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public float getAscendingNode() {
        return ascendingNode;
    }

    public float getMeanAnomaly() {
        return meanAnomaly;
    }

    public float getRotationPeriod() {
        return rotationPeriod;
    }

    public float getAxialTilt() {
        return axialTilt;
    }

    public boolean isWasDiscovered() {
        return wasDiscovered;
    }

    public boolean isWasMapped() {
        return wasMapped;
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

    public List<AtmosphereComposition> getAtmosphereComposition() {
        return atmosphereComposition;
    }

    public String getVolcanism() {
        return volcanism;
    }

    public float getMassEM() {
        return massEM;
    }

    public float getSurfaceGravity() {
        return surfaceGravity;
    }

    public float getSurfacePressure() {
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

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    public static class Parent {
        @SerializedName("Null")
        private Integer nullId;

        @SerializedName("Star")
        private Integer starId;

        public Integer getNullId() {
            return nullId;
        }

        public Integer getStarId() {
            return starId;
        }
    }

    public static class AtmosphereComposition {
        @SerializedName("Name")
        private String name;

        @SerializedName("Percent")
        private float percent;

        public String getName() {
            return name;
        }

        public float getPercent() {
            return percent;
        }
    }

    public static class Material {
        @SerializedName("Name")
        private String name;

        @SerializedName("Percent")
        private float percent;

        public String getName() {
            return name;
        }

        public float getPercent() {
            return percent;
        }
    }

    public static class Composition {
        @SerializedName("Ice")
        private float ice;

        @SerializedName("Rock")
        private float rock;

        @SerializedName("Metal")
        private float metal;

        public float getIce() {
            return ice;
        }

        public float getRock() {
            return rock;
        }

        public float getMetal() {
            return metal;
        }
    }

    @Override
    public String toString() {
        return new StringJoiner("Sensors detected: ")
                .add("scanType='" + scanType + "'")
                .add("bodyName='" + bodyName + "'")
                .add("bodyId=" + bodyId)
                .add("parents=" + parents)
                .add("starSystem='" + starSystem + "'")
                .add("systemAddress=" + systemAddress)
                .add("distanceFromArrivalLS=" + distanceFromArrivalLS)
                .add("starType='" + starType + "'")
                .add("subclass=" + subclass)
                .add("stellarMass=" + stellarMass)
                .add("radius=" + radius)
                .add("absoluteMagnitude=" + absoluteMagnitude)
                .add("ageMY=" + ageMY)
                .add("surfaceTemperature=" + surfaceTemperature)
                .add("luminosity='" + luminosity + "'")
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
                .add("tidalLock=" + tidalLock)
                .add("terraformState='" + terraformState + "'")
                .add("planetClass='" + planetClass + "'")
                .add("atmosphere='" + atmosphere + "'")
                .add("atmosphereType='" + atmosphereType + "'")
                .add("atmosphereComposition=" + atmosphereComposition)
                .add("volcanism='" + volcanism + "'")
                .add("massEM=" + massEM)
                .add("surfaceGravity=" + surfaceGravity)
                .add("surfacePressure=" + surfacePressure)
                .add("landable=" + landable)
                .add("materials=" + materials)
                .add("composition=" + composition)
                .toString();
    }
}