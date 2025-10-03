package elite.intel.gameapi.journal.events.dto;

import elite.intel.ai.search.edsm.dto.*;
import elite.intel.ai.search.edsm.dto.data.BodyData;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;

public class LocationDto implements ToJsonConvertible {

    private List<CodexEntryEvent> codexEntries = new ArrayList<>();
    private List<MaterialDto> materials = new ArrayList<>();
    private List<SAASignalsFoundEvent.Signal> saaSignals = new ArrayList<>();
    private List<FSSBodySignalsEvent.Signal> fssSignals;
    private List<GenusDto> genus = new ArrayList<>();
    private List<BioSampleDto> partialBioSamples = new ArrayList<>();
    private List<String> stationServices;
    private List<String> powers;
    private Set<FssSignalDto> detectedSignals = new HashSet<>();
    private double X;
    private double Y;
    private double Z;
    private double distance;
    private String starName;
    private String starType;
    private boolean isFuelStar;
    private String planetName="";
    private String planetShortName="";
    private BodyData planetData;
    private TrafficDto trafficDto;
    private DeathsDto deathsDto;
    private String stationName;
    private String stationType;
    private long marketID;
    private String stationFaction;
    private String stationGovernment;
    private String stationAllegiance;
    private String stationEconomy;
    private String allegiance;
    private String economy;
    private String secondEconomy;
    private String government;
    private String security;
    private long population;
    private String bodyType;
    private String controllingPower;
    private String powerplayState;
    private double powerplayStateControlProgress;
    private int powerplayStateReinforcement;
    private int powerplayStateUndermining;
    private double gravity;
    private double surfaceTemperature;
    private double[] landingCoordinates;
    private double orbitalCruiseEntryAltitude;
    private boolean ourDiscovery = false;
    private boolean weMappedIt = false;

    //private BioStatus bioStatus = BioStatus.SCAN_REQUIRED;
    private MarketDto market;
    private OutfittingDto outfitting;
    private ShipyardDto shipyard;

    public LocationDto(long id) {
        setBodyId(id);
    }

    public void setMarket(MarketDto marketDto) {
        this.market = marketDto;
    }

    public MarketDto getMarket() {
        return market;
    }

    public void setOutfitting(OutfittingDto outfittingDto) {
        this.outfitting = outfittingDto;
    }

    public OutfittingDto getOutfitting() {
        return outfitting;
    }

    public void setShipyard(ShipyardDto shipyardDto) {
        this.shipyard= shipyardDto;
    }

    public ShipyardDto getShipyard() {
        return shipyard;
    }

    public void deletePartialBioSamples() {
        partialBioSamples.clear();
    }

    public enum BioStatus {
        BIO_FORMS_PRESENT,
        NO_BIO_FORMS,
        SCAN_REQUIRED;
    }

    private LocationType locationType = LocationType.UNKNOWN;
    public enum LocationType {
        STAR,
        PRIMARY_STAR,
        PLANET_OR_MOON,
        PLANETARY_RING,
        BLACK_HOLE,
        NEBULA,
        STATION,
        FLEET_CARRIER,
        FACILITY,
        MARKET,
        BELT_CLUSTER,
        UNKNOWN
    }

    private long bodyId;
    private boolean isLandable;
    private String planetClass;
    private boolean isTerraformable;
    private boolean isTidalLocked;
    private String atmosphere;
    private double radius;
    private double massEM;
    private int bioSignals;
    private int geoSignals;
    private boolean hasRings;

    private LocationDto() {

    }


    public LocationDto(LocationType locationType) {
        setLocationType(locationType);
    }


    public double getX() {
        return X;
    }

    public void setX(double x) {
        if(x == 0) return;
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        if(y == 0) return;
        Y = y;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double z) {
        if(z == 0) return;
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

    public void addSaaSignals(List<SAASignalsFoundEvent.Signal> signals) {
        if (this.saaSignals == null) {signals = new ArrayList<>();}
        this.saaSignals.addAll(signals);
    }

    public List<GenusDto> getGenus() {
        return genus;
    }


    public void setGenus(List<GenusDto> genus) {
        this.genus = genus;
    }

    public List<BioSampleDto> getPartialBioSamples() {
        return partialBioSamples;
    }

    public void addDetectedSignal(FssSignalDto signal) {
        this.detectedSignals.add(signal);
    }

    public void addDetectedSignals(List<FssSignalDto> signals) {
        this.detectedSignals.addAll(signals);
    }


    public Set<FssSignalDto> getDetectedSignals() {
        return detectedSignals;
    }

    public void addBioScan(BioSampleDto bioSampleDto) {
        this.partialBioSamples.add(bioSampleDto);
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



    public void setPartialBioSamples(List<BioSampleDto> partialBioSamples) {
        this.partialBioSamples = partialBioSamples;
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


    public String getAllegiance() {
        return allegiance;
    }

    public void setAllegiance(String allegiance) {
        this.allegiance = allegiance;
    }

    public String getEconomy() {
        return economy;
    }

    public void setEconomy(String economy) {
        this.economy = economy;
    }

    public String getSecondEconomy() {
        return secondEconomy;
    }

    public void setSecondEconomy(String secondEconomy) {
        this.secondEconomy = secondEconomy;
    }

    public String getGovernment() {
        return government;
    }

    public void setGovernment(String government) {
        this.government = government;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
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

    public void addMaterial(MaterialDto materialDto) {
        this.materials.add(materialDto);
    }

    public List<MaterialDto> getMaterials() {
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


    public double[] getLandingCoordinates() {
        return landingCoordinates;
    }

    public void setLandingCoordinates(double[] landingCoordinates) {
        this.landingCoordinates = landingCoordinates;
    }

    public double getOrbitalCruiseEntryAltitude() {
        return orbitalCruiseEntryAltitude;
    }

    public void setOrbitalCruiseEntryAltitude(double orbitalCruiseEntryAltitude) {
        this.orbitalCruiseEntryAltitude = orbitalCruiseEntryAltitude;
    }

    public boolean isOurDiscovery() {
        return ourDiscovery;
    }

    public void setOurDiscovery(boolean ourDiscovery) {
        this.ourDiscovery = ourDiscovery;
    }


    public void setMaterials(List<MaterialDto> materials) {
        this.materials = materials;
    }

    public List<SAASignalsFoundEvent.Signal> getSaaSignals() {
        return saaSignals;
    }

    public void setSaaSignals(List<SAASignalsFoundEvent.Signal> saaSignals) {
        this.saaSignals = saaSignals;
    }

    public List<FSSBodySignalsEvent.Signal> getFssSignals() {
        return fssSignals;
    }

    public void setFssSignals(List<FSSBodySignalsEvent.Signal> fssSignals) {
        this.fssSignals = fssSignals;
    }

    public void setDetectedSignals(Set<FssSignalDto> detectedSignals) {
        this.detectedSignals = detectedSignals;
    }

/*
    public BioStatus getBioStatus() {
        return bioStatus;
    }

    public void setBioStatus(BioStatus bioStatus) {
        this.bioStatus = bioStatus;
    }
*/

    public long getBodyId() {
        return bodyId;
    }

    public void setBodyId(long bodyId) {
        this.bodyId = bodyId;
    }

    public boolean isLandable() {
        return isLandable;
    }

    public void setLandable(boolean landable) {
        isLandable = landable;
    }

    public String getPlanetClass() {
        return planetClass;
    }

    public void setPlanetClass(String planetClass) {
        this.planetClass = planetClass;
    }

    public boolean isTerraformable() {
        return isTerraformable;
    }

    public void setTerraformable(boolean terraformable) {
        isTerraformable = terraformable;
    }

    public boolean isTidalLocked() {
        return isTidalLocked;
    }

    public void setTidalLocked(boolean tidalLocked) {
        isTidalLocked = tidalLocked;
    }

    public String getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(String atmosphere) {
        this.atmosphere = atmosphere;
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

    public int getBioSignals() {
        return bioSignals;
    }

    public void setBioSignals(int bioSignals) {
        this.bioSignals = bioSignals;
    }

    public int getGeoSignals() {
        return geoSignals;
    }

    public void setGeoSignals(int geoSignals) {
        this.geoSignals = geoSignals;
    }

    public boolean isHasRings() {
        return hasRings;
    }

    public void setHasRings(boolean hasRings) {
        this.hasRings = hasRings;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public List<CodexEntryEvent> getCodexEntries() {
        return codexEntries;
    }

    public void setCodexEntries(List<CodexEntryEvent> codexEntries) {
        this.codexEntries = codexEntries;
    }

    public void addCodexEntry(CodexEntryEvent entry) {
        this.codexEntries.add(entry);
    }

    public void clearCodexEntries() {
        this.codexEntries.clear();
    }

    public boolean isWeMappedIt() {
        return weMappedIt;
    }

    public void setWeMappedIt(boolean weMappedIt) {
        this.weMappedIt = weMappedIt;
    }

    public String getStarType() {
        return starType;
    }

    public void setStarType(String starType) {
        this.starType = starType;
        if(starType != null && "KGBFOAM".contains(starType)) {
            setFuelStar(true);
        }
    }

    public boolean isFuelStar() {
        return isFuelStar;
    }

    public void setFuelStar(boolean fuelStar) {
        isFuelStar = fuelStar;
    }


    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        LocationDto that = (LocationDto) o;
        return Double.compare(getX(), that.getX()) == 0 && Double.compare(getY(), that.getY()) == 0 && Double.compare(getZ(), that.getZ()) == 0 && Double.compare(getDistance(), that.getDistance()) == 0 && isFuelStar() == that.isFuelStar() && getMarketID() == that.getMarketID() && getPopulation() == that.getPopulation() && Double.compare(getPowerplayStateControlProgress(), that.getPowerplayStateControlProgress()) == 0 && getPowerplayStateReinforcement() == that.getPowerplayStateReinforcement() && getPowerplayStateUndermining() == that.getPowerplayStateUndermining() && Double.compare(getGravity(), that.getGravity()) == 0 && Double.compare(getSurfaceTemperature(), that.getSurfaceTemperature()) == 0 && Double.compare(getOrbitalCruiseEntryAltitude(), that.getOrbitalCruiseEntryAltitude()) == 0 && isOurDiscovery() == that.isOurDiscovery() && isWeMappedIt() == that.isWeMappedIt() && getBodyId() == that.getBodyId() && isLandable() == that.isLandable() && isTerraformable() == that.isTerraformable() && isTidalLocked() == that.isTidalLocked() && Double.compare(getRadius(), that.getRadius()) == 0 && Double.compare(getMassEM(), that.getMassEM()) == 0 && getBioSignals() == that.getBioSignals() && getGeoSignals() == that.getGeoSignals() && isHasRings() == that.isHasRings() && Objects.equals(getCodexEntries(), that.getCodexEntries()) && Objects.equals(getMaterials(), that.getMaterials()) && Objects.equals(getSaaSignals(), that.getSaaSignals()) && Objects.equals(getFssSignals(), that.getFssSignals()) && Objects.equals(getGenus(), that.getGenus()) && Objects.equals(getPartialBioSamples(), that.getPartialBioSamples()) && Objects.equals(getStationServices(), that.getStationServices()) && Objects.equals(getPowers(), that.getPowers()) && Objects.equals(getDetectedSignals(), that.getDetectedSignals()) && Objects.equals(getStarName(), that.getStarName()) && Objects.equals(getStarType(), that.getStarType()) && Objects.equals(getPlanetName(), that.getPlanetName()) && Objects.equals(getPlanetShortName(), that.getPlanetShortName()) && Objects.equals(getPlanetData(), that.getPlanetData()) && Objects.equals(getTrafficDto(), that.getTrafficDto()) && Objects.equals(getDeathsDto(), that.getDeathsDto()) && Objects.equals(getStationName(), that.getStationName()) && Objects.equals(getStationType(), that.getStationType()) && Objects.equals(getStationFaction(), that.getStationFaction()) && Objects.equals(getStationGovernment(), that.getStationGovernment()) && Objects.equals(getStationAllegiance(), that.getStationAllegiance()) && Objects.equals(getStationEconomy(), that.getStationEconomy()) && Objects.equals(getAllegiance(), that.getAllegiance()) && Objects.equals(getEconomy(), that.getEconomy()) && Objects.equals(getSecondEconomy(), that.getSecondEconomy()) && Objects.equals(getGovernment(), that.getGovernment()) && Objects.equals(getSecurity(), that.getSecurity()) && Objects.equals(getBodyType(), that.getBodyType()) && Objects.equals(getControllingPower(), that.getControllingPower()) && Objects.equals(getPowerplayState(), that.getPowerplayState()) && Arrays.equals(getLandingCoordinates(), that.getLandingCoordinates()) && Objects.equals(getMarket(), that.getMarket()) && Objects.equals(getOutfitting(), that.getOutfitting()) && Objects.equals(getShipyard(), that.getShipyard()) && getLocationType() == that.getLocationType() && Objects.equals(getPlanetClass(), that.getPlanetClass()) && Objects.equals(getAtmosphere(), that.getAtmosphere());
    }

    @Override public int hashCode() {
        int result = Objects.hashCode(getCodexEntries());
        result = 31 * result + Objects.hashCode(getMaterials());
        result = 31 * result + Objects.hashCode(getSaaSignals());
        result = 31 * result + Objects.hashCode(getFssSignals());
        result = 31 * result + Objects.hashCode(getGenus());
        result = 31 * result + Objects.hashCode(getPartialBioSamples());
        result = 31 * result + Objects.hashCode(getStationServices());
        result = 31 * result + Objects.hashCode(getPowers());
        result = 31 * result + Objects.hashCode(getDetectedSignals());
        result = 31 * result + Double.hashCode(getX());
        result = 31 * result + Double.hashCode(getY());
        result = 31 * result + Double.hashCode(getZ());
        result = 31 * result + Double.hashCode(getDistance());
        result = 31 * result + Objects.hashCode(getStarName());
        result = 31 * result + Objects.hashCode(getStarType());
        result = 31 * result + Boolean.hashCode(isFuelStar());
        result = 31 * result + Objects.hashCode(getPlanetName());
        result = 31 * result + Objects.hashCode(getPlanetShortName());
        result = 31 * result + Objects.hashCode(getPlanetData());
        result = 31 * result + Objects.hashCode(getTrafficDto());
        result = 31 * result + Objects.hashCode(getDeathsDto());
        result = 31 * result + Objects.hashCode(getStationName());
        result = 31 * result + Objects.hashCode(getStationType());
        result = 31 * result + Long.hashCode(getMarketID());
        result = 31 * result + Objects.hashCode(getStationFaction());
        result = 31 * result + Objects.hashCode(getStationGovernment());
        result = 31 * result + Objects.hashCode(getStationAllegiance());
        result = 31 * result + Objects.hashCode(getStationEconomy());
        result = 31 * result + Objects.hashCode(getAllegiance());
        result = 31 * result + Objects.hashCode(getEconomy());
        result = 31 * result + Objects.hashCode(getSecondEconomy());
        result = 31 * result + Objects.hashCode(getGovernment());
        result = 31 * result + Objects.hashCode(getSecurity());
        result = 31 * result + Long.hashCode(getPopulation());
        result = 31 * result + Objects.hashCode(getBodyType());
        result = 31 * result + Objects.hashCode(getControllingPower());
        result = 31 * result + Objects.hashCode(getPowerplayState());
        result = 31 * result + Double.hashCode(getPowerplayStateControlProgress());
        result = 31 * result + getPowerplayStateReinforcement();
        result = 31 * result + getPowerplayStateUndermining();
        result = 31 * result + Double.hashCode(getGravity());
        result = 31 * result + Double.hashCode(getSurfaceTemperature());
        result = 31 * result + Arrays.hashCode(getLandingCoordinates());
        result = 31 * result + Double.hashCode(getOrbitalCruiseEntryAltitude());
        result = 31 * result + Boolean.hashCode(isOurDiscovery());
        result = 31 * result + Boolean.hashCode(isWeMappedIt());
        result = 31 * result + Objects.hashCode(getMarket());
        result = 31 * result + Objects.hashCode(getOutfitting());
        result = 31 * result + Objects.hashCode(getShipyard());
        result = 31 * result + Objects.hashCode(getLocationType());
        result = 31 * result + Long.hashCode(getBodyId());
        result = 31 * result + Boolean.hashCode(isLandable());
        result = 31 * result + Objects.hashCode(getPlanetClass());
        result = 31 * result + Boolean.hashCode(isTerraformable());
        result = 31 * result + Boolean.hashCode(isTidalLocked());
        result = 31 * result + Objects.hashCode(getAtmosphere());
        result = 31 * result + Double.hashCode(getRadius());
        result = 31 * result + Double.hashCode(getMassEM());
        result = 31 * result + getBioSignals();
        result = 31 * result + getGeoSignals();
        result = 31 * result + Boolean.hashCode(isHasRings());
        return result;
    }
}