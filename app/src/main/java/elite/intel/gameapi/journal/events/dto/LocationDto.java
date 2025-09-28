package elite.intel.gameapi.journal.events.dto;

import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.ai.search.edsm.dto.data.BodyData;
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
    private String planetName="";
    private String planetShortName="";
    BodyData planetData;
    List<SAASignalsFoundEvent.Signal> signals = new ArrayList<>();
    List<GenusDto> genus = new ArrayList<>();
    List<BioSampleDto> bioScans = new ArrayList<>();
    Set<FssSignal> detectedSignals = new HashSet<>();
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
    private double[] landingCoordinates;
    private double orbitalCruiseEntryAltitude;
    private BioStatus bioStatus = BioStatus.SCAN_REQUIRED;
    public enum BioStatus {
        BIO_FORMS_PRESENT,
        NO_BIO_FORMS,
        SCAN_REQUIRED;
    }




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

    public List<SAASignalsFoundEvent.Signal> getSaaSignals() {
        return signals;
    }

    public void addSaaSignals(List<SAASignalsFoundEvent.Signal> signals) {
        if (this.signals == null) {signals = new ArrayList<>();}
        this.signals.addAll(signals);
    }

    public List<GenusDto> getGenus() {
        return genus;
    }

    public void clearGenus() {
        this.genus.clear();
    }

    public void setGenus(List<GenusDto> genus) {
        this.genus = genus;
    }

    public List<BioSampleDto> getBioScans() {
        return bioScans;
    }

    public void addDetectedSignal(FssSignal signal) {
        this.detectedSignals.add(signal);
    }


    public Set<FssSignal> getDetectedSignals() {
        return detectedSignals;
    }

    public void addBioScan(BioSampleDto bioSampleDto) {
        this.bioScans.add(bioSampleDto);
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


    public void clearSaaSignals() {
        this.signals.clear();
    }

    public void setBioScans(List<BioSampleDto> bioScans) {
        this.bioScans = bioScans;
    }

    public void clearDetectedSignals() {
        this.detectedSignals.clear();
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


    public String getPlanetShortName() {
        return planetShortName;
    }

    public void setPlanetShortName(String planetShortName) {
        this.planetShortName = planetShortName;
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    public void clearBioSamples() {
        this.bioScans.clear();
    }

    public double[] getLandingCoordinates() {
        return landingCoordinates;
    }

    public void setLandingCoordinates(double[] landingCoordinates) {
        this.landingCoordinates = landingCoordinates;
    }

    public void setBioFormsPresent(boolean bioFormsPresent) {
        if(bioFormsPresent) bioStatus = BioStatus.BIO_FORMS_PRESENT;
        if(!bioFormsPresent) bioStatus = BioStatus.NO_BIO_FORMS;
    }

    public double getOrbitalCruiseEntryAltitude() {
        return orbitalCruiseEntryAltitude;
    }

    public void setOrbitalCruiseEntryAltitude(double orbitalCruiseEntryAltitude) {
        this.orbitalCruiseEntryAltitude = orbitalCruiseEntryAltitude;
    }
}