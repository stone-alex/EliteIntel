package elite.intel.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class BodyData {
    @SerializedName("id")
    public int id;
    @SerializedName("id64")
    public long id64;
    @SerializedName("bodyId")
    public int bodyId;
    @SerializedName("name")
    public String name;
    @SerializedName("discovery")
    public DiscoveryData discovery;
    @SerializedName("type")
    public String type;
    @SerializedName("subType")
    public String subType;
    @SerializedName("parents")
    public List<ParentBody> parents;
    @SerializedName("distanceToArrival")
    public double distanceToArrival;
    @SerializedName("isMainStar")
    public boolean isMainStar;
    @SerializedName("isScoopable")
    public boolean isScoopable;
    @SerializedName("age")
    public int age;
    @SerializedName("spectralClass")
    public String spectralClass;
    @SerializedName("luminosity")
    public String luminosity;
    @SerializedName("absoluteMagnitude")
    public double absoluteMagnitude;
    @SerializedName("solarMasses")
    public double solarMasses;
    @SerializedName("solarRadius")
    public double solarRadius;
    @SerializedName("surfaceTemperature")
    public int surfaceTemperature;
    @SerializedName("orbitalPeriod")
    public Double orbitalPeriod;
    @SerializedName("semiMajorAxis")
    public Double semiMajorAxis;
    @SerializedName("orbitalEccentricity")
    public Double orbitalEccentricity;
    @SerializedName("orbitalInclination")
    public Double orbitalInclination;
    @SerializedName("argOfPeriapsis")
    public Double argOfPeriapsis;
    @SerializedName("rotationalPeriod")
    public double rotationalPeriod;
    @SerializedName("rotationalPeriodTidallyLocked")
    public boolean rotationalPeriodTidallyLocked;
    @SerializedName("axialTilt")
    public double axialTilt;
    @SerializedName("belts")
    public List<BeltData> belts;
    @SerializedName("rings")
    public List<RingData> rings;
    @SerializedName("updateTime")
    public String updateTime;
    @SerializedName("materials")
    public Map<String, Double> materials;
    @SerializedName("isLandable")
    public boolean isLandable;
    @SerializedName("gravity")
    public double gravity;
    @SerializedName("earthMasses")
    public double earthMasses;
    @SerializedName("radius")
    public double radius;
    @SerializedName("surfacePressure")
    public Double surfacePressure;
    @SerializedName("volcanismType")
    public String volcanismType;
    @SerializedName("atmosphereType")
    public String atmosphereType;
    @SerializedName("atmosphereComposition")
    public Map<String, Double> atmosphereComposition;
    @SerializedName("solidComposition")
    public Map<String, Double> solidComposition;
    @SerializedName("terraformingState")
    public String terraformingState;

    public int getId() {
        return id;
    }

    public long getId64() {
        return id64;
    }

    public int getBodyId() {
        return bodyId;
    }

    public String getName() {
        return name;
    }

    public DiscoveryData getDiscovery() {
        return discovery;
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public List<ParentBody> getParents() {
        return parents;
    }

    public double getDistanceToArrival() {
        return distanceToArrival;
    }

    public boolean isMainStar() {
        return isMainStar;
    }

    public boolean isScoopable() {
        return isScoopable;
    }

    public int getAge() {
        return age;
    }

    public String getSpectralClass() {
        return spectralClass;
    }

    public String getLuminosity() {
        return luminosity;
    }

    public double getAbsoluteMagnitude() {
        return absoluteMagnitude;
    }

    public double getSolarMasses() {
        return solarMasses;
    }

    public double getSolarRadius() {
        return solarRadius;
    }

    public int getSurfaceTemperature() {
        return surfaceTemperature;
    }

    public Double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public Double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public Double getOrbitalEccentricity() {
        return orbitalEccentricity;
    }

    public Double getOrbitalInclination() {
        return orbitalInclination;
    }

    public Double getArgOfPeriapsis() {
        return argOfPeriapsis;
    }

    public double getRotationalPeriod() {
        return rotationalPeriod;
    }

    public boolean isRotationalPeriodTidallyLocked() {
        return rotationalPeriodTidallyLocked;
    }

    public double getAxialTilt() {
        return axialTilt;
    }

    public List<BeltData> getBelts() {
        return belts;
    }

    public List<RingData> getRings() {
        return rings;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public Map<String, Double> getMaterials() {
        return materials;
    }

    public boolean isLandable() {
        return isLandable;
    }

    public double getGravity() {
        return gravity;
    }

    public double getEarthMasses() {
        return earthMasses;
    }

    public double getRadius() {
        return radius;
    }

    public Double getSurfacePressure() {
        return surfacePressure;
    }

    public String getVolcanismType() {
        return volcanismType;
    }

    public String getAtmosphereType() {
        return atmosphereType;
    }

    public Map<String, Double> getAtmosphereComposition() {
        return atmosphereComposition;
    }

    public Map<String, Double> getSolidComposition() {
        return solidComposition;
    }

    public String getTerraformingState() {
        return terraformingState;
    }
}
