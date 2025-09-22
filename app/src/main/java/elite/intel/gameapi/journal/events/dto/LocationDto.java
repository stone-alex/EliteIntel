package elite.intel.gameapi.journal.events.dto;

import elite.intel.ai.search.edsm.dto.data.BodyData;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    public void addBioScan(BioSampleDto bioSampleDto) {
        this.completedBioScans.add(bioSampleDto);
    }
}