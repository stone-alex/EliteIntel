package elite.intel.gameapi.journal.events.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.ai.search.edsm.dto.data.BodyData;
import elite.intel.gameapi.journal.events.LocationEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;

public class LocationDto implements ToJsonConvertible {

    private double X;
    private double Y;
    private double Z;
    private double distance;
    private String starName;
    private String allegiance;
    private String security;
    private String government;
    private String planetName;
    BodyData planetData;
    List<SAASignalsFoundEvent.Signal> signals;
    List<SAASignalsFoundEvent.Genus> genus;
    List<BioSampleDto> completedBioScans = new ArrayList<>();
    Set<String> detectedSignals = new HashSet<>();
    TrafficDto trafficDto;
    DeathsDto deathsDto;

    private String stationName;
    private String stationType;
    private long marketID;
    private String stationFaction;
    private String stationGovernment;
    private String stationAllegiance;
    private List<String> stationServices;
    private String stationEconomy;
    private double[] starPos;
    private String systemAllegiance;
    private String systemEconomy;
    private String systemSecondEconomy;
    private String systemGovernment;
    private String systemSecurity;
    private long population;
    private String bodyType;
    private String controllingPower;
    private List<String> powers;
    private String powerplayState;
    private double powerplayStateControlProgress;
    private int powerplayStateReinforcement;
    private int powerplayStateUndermining;
    private double gravity;
    private double surfaceTemperature;
    Map<String, Double> materials = new HashMap<>();




    public double getX() {
        return X;
    }

    public void setX(double x) {
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        Y = y;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double z) {
        Z = z;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String getAllegiance() {
        return allegiance;
    }

    public void setAllegiance(String allegiance) {
        this.allegiance = allegiance;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getGovernment() {
        return government;
    }

    public void setGovernment(String government) {
        this.government = government;
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    public BodyData getPlanetData() {
        return planetData;
    }

    public void setPlanetData(BodyData planetData) {
        this.planetData = planetData;
    }

    public List<SAASignalsFoundEvent.Signal> getSignals() {
        return signals;
    }

    public void addSignals(List<SAASignalsFoundEvent.Signal> signals) {
        this.signals.addAll(signals);
    }

    public List<SAASignalsFoundEvent.Genus> getGenus() {
        return genus;
    }

    public void setGenus(List<SAASignalsFoundEvent.Genus> genus) {
        this.genus = genus;
    }

    public List<BioSampleDto> getCompletedBioScans() {
        return completedBioScans;
    }

    public void addDetectedSignal(String signal) {
        this.detectedSignals.add(signal);
    }


    public Set<String> getDetectedSignals() {
        return detectedSignals;
    }

    public void addBioScan(BioSampleDto bioSampleDto) {
        this.completedBioScans.add(bioSampleDto);
    }

    public DeathsDto getDeathsDto() {
        return deathsDto;
    }

    public void setDeathsDto(DeathsDto deathsDto) {
        this.deathsDto = deathsDto;
    }

    public TrafficDto getTrafficDto() {
        return trafficDto;
    }

    public void setTrafficDto(TrafficDto trafficDto) {
        this.trafficDto = trafficDto;
    }


    public void setSignals(List<SAASignalsFoundEvent.Signal> signals) {
        this.signals = signals;
    }

    public void setCompletedBioScans(List<BioSampleDto> completedBioScans) {
        this.completedBioScans = completedBioScans;
    }

    public void setDetectedSignals(Set<String> detectedSignals) {
        this.detectedSignals = detectedSignals;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public long getMarketID() {
        return marketID;
    }

    public void setMarketID(long marketID) {
        this.marketID = marketID;
    }

    public String getStationFaction() {
        return stationFaction;
    }

    public void setStationFaction(String stationFaction) {
        this.stationFaction = stationFaction;
    }

    public String getStationGovernment() {
        return stationGovernment;
    }

    public void setStationGovernment(String stationGovernment) {
        this.stationGovernment = stationGovernment;
    }

    public String getStationAllegiance() {
        return stationAllegiance;
    }

    public void setStationAllegiance(String stationAllegiance) {
        this.stationAllegiance = stationAllegiance;
    }

    public List<String> getStationServices() {
        return stationServices;
    }

    public void setStationServices(List<String> stationServices) {
        this.stationServices = stationServices;
    }

    public String getStationEconomy() {
        return stationEconomy;
    }

    public void setStationEconomy(String stationEconomy) {
        this.stationEconomy = stationEconomy;
    }

    public double[] getStarPos() {
        return starPos;
    }

    public void setStarPos(double[] starPos) {
        this.starPos = starPos;
    }

    public String getSystemAllegiance() {
        return systemAllegiance;
    }

    public void setSystemAllegiance(String systemAllegiance) {
        this.systemAllegiance = systemAllegiance;
    }

    public String getSystemEconomy() {
        return systemEconomy;
    }

    public void setSystemEconomy(String systemEconomy) {
        this.systemEconomy = systemEconomy;
    }

    public String getSystemSecondEconomy() {
        return systemSecondEconomy;
    }

    public void setSystemSecondEconomy(String systemSecondEconomy) {
        this.systemSecondEconomy = systemSecondEconomy;
    }

    public String getSystemGovernment() {
        return systemGovernment;
    }

    public void setSystemGovernment(String systemGovernment) {
        this.systemGovernment = systemGovernment;
    }

    public String getSystemSecurity() {
        return systemSecurity;
    }

    public void setSystemSecurity(String systemSecurity) {
        this.systemSecurity = systemSecurity;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getControllingPower() {
        return controllingPower;
    }

    public void setControllingPower(String controllingPower) {
        this.controllingPower = controllingPower;
    }

    public List<String> getPowers() {
        return powers;
    }

    public void setPowers(List<String> powers) {
        this.powers = powers;
    }

    public String getPowerplayState() {
        return powerplayState;
    }

    public void setPowerplayState(String powerplayState) {
        this.powerplayState = powerplayState;
    }

    public double getPowerplayStateControlProgress() {
        return powerplayStateControlProgress;
    }

    public void setPowerplayStateControlProgress(double powerplayStateControlProgress) {
        this.powerplayStateControlProgress = powerplayStateControlProgress;
    }

    public int getPowerplayStateReinforcement() {
        return powerplayStateReinforcement;
    }

    public void setPowerplayStateReinforcement(int powerplayStateReinforcement) {
        this.powerplayStateReinforcement = powerplayStateReinforcement;
    }

    public int getPowerplayStateUndermining() {
        return powerplayStateUndermining;
    }

    public void setPowerplayStateUndermining(int powerplayStateUndermining) {
        this.powerplayStateUndermining = powerplayStateUndermining;
    }


    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getSurfaceTemperature() {
        return surfaceTemperature;
    }

    public void setSurfaceTemperature(double surfaceTemperature) {
        this.surfaceTemperature = surfaceTemperature;
    }

    public void addMaterial(String materialName, double materialPercentage) {
        this.materials.put(materialName, materialPercentage);
    }

    public Map<String, Double> getMaterials() {
        return materials;
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}