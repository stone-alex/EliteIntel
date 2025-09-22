package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.gamestate.events.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class BioSampleDto extends BaseJsonDto implements ToJsonConvertible {

    private String genus;
    private double scanLatitude;
    private double scanLongitude;
    private double distanceToNextSample;
    private boolean playerFarEnough;
    private boolean bioSampleCompleted;

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
}
