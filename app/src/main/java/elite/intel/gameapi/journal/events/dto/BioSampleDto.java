package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.gamestate.events.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class BioSampleDto extends BaseJsonDto implements ToJsonConvertible {

    private String planetName;
    private String planetShortName;

    private long  bodyId;
    private String genus;
    private double scanLatitude;
    private double scanLongitude;
    private double distanceToNextSample;
    private boolean playerFarEnough;
    private boolean bioSampleCompleted;
    private String species;
    int planetNumber;
    long starSystemNumber;
    long payout;
    long fistDiscoveryBonus;
    boolean ourDiscovery;

    private String scanXof3;

    public long getBodyId() {
        return bodyId;
    }

    public void setBodyId(long bodyId) {
        this.bodyId = bodyId;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public double getScanLatitude() {
        return scanLatitude;
    }

    public void setScanLatitude(double scanLatitude) {
        this.scanLatitude = scanLatitude;
    }

    public double getScanLongitude() {
        return scanLongitude;
    }

    public void setScanLongitude(double scanLongitude) {
        this.scanLongitude = scanLongitude;
    }

    public double getDistanceToNextSample() {
        return distanceToNextSample;
    }

    public void setDistanceToNextSample(double distanceToNextSample) {
        this.distanceToNextSample = distanceToNextSample;
    }

    public boolean isPlayerFarEnough() {
        return playerFarEnough;
    }

    public void setPlayerFarEnough(boolean playerFarEnough) {
        this.playerFarEnough = playerFarEnough;
    }

    public boolean isBioSampleCompleted() {
        return bioSampleCompleted;
    }

    public void setBioSampleCompleted(boolean bioSampleCompleted) {
        this.bioSampleCompleted = bioSampleCompleted;
    }

    public void setSpecies(String variant) {
        this.species = variant;
    }

    public String getSpecies() {
        return species;
    }

    public int getPlanetNumber() {
        return planetNumber;
    }

    public void setPlanetNumber(int planetNumber) {
        this.planetNumber = planetNumber;
    }

    public long getStarSystemNumber() {
        return starSystemNumber;
    }

    public void setStarSystemNumber(long starSystemNumber) {
        this.starSystemNumber = starSystemNumber;
    }

    public long getPayout() {
        return payout;
    }

    public void setPayout(long payout) {
        this.payout = payout;
    }

    public String getScanXof3() {
        return scanXof3;
    }

    public void setScanXof3(String scanXof3) {
        this.scanXof3 = scanXof3;
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    public long getFistDiscoveryBonus() {
        return fistDiscoveryBonus;
    }

    public void setFistDiscoveryBonus(long fistDiscoveryBonus) {
        this.fistDiscoveryBonus = fistDiscoveryBonus;
    }

    public boolean isOurDiscovery() {
        return ourDiscovery;
    }

    public void setOurDiscovery(boolean ourDiscovery) {
        this.ourDiscovery = ourDiscovery;
    }

    public String getPlanetShortName() {
        return planetShortName;
    }

    public void setPlanetShortName(String planetShortName) {
        this.planetShortName = planetShortName;
    }
}
