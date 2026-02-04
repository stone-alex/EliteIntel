package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.Md5Utils;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class BioSampleDto extends BaseJsonDto implements ToJsonConvertible, ToYamlConvertable {

    int planetNumber;
    long payout;
    long fistDiscoveryBonus;
    boolean ourDiscovery;
    private String planetName;
    private String planetShortName;
    private String primaryStar;
    private long bodyId;
    private String genus;
    private double scanLatitude;
    private double scanLongitude;
    private double distanceToNextSample;
    private boolean playerFarEnough;
    private boolean bioSampleCompleted;
    private String species;
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

    public String getPrimaryStar() {
        return primaryStar;
    }

    public void setPrimaryStar(String primaryStar) {
        this.primaryStar = primaryStar;
    }

    public String getKey() {
        return Md5Utils.generateMd5(bodyId + planetName + genus + species);
    }

    @Override public String toYaml() {
        return YamlFactory.toYaml(this);
    }
}
