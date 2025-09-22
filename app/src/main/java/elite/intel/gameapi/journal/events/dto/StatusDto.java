package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.gamestate.events.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class StatusDto extends BaseJsonDto implements ToJsonConvertible {

    private int[] pips;
    private int fireGroup;
    private int guiFocus;
    private double cargo;
    private String legalState;
    private double latitude;
    private double longitude;
    private int heading;
    private double altitude;
    private long balance;
    private double planetRadius;

    public int[] getPips() {
        return pips;
    }

    public void setPips(int[] pips) {
        this.pips = pips;
    }

    public int getFireGroup() {
        return fireGroup;
    }

    public void setFireGroup(int fireGroup) {
        this.fireGroup = fireGroup;
    }

    public int getGuiFocus() {
        return guiFocus;
    }

    public void setGuiFocus(int guiFocus) {
        this.guiFocus = guiFocus;
    }

    public double getCargo() {
        return cargo;
    }

    public void setCargo(double cargo) {
        this.cargo = cargo;
    }

    public String getLegalState() {
        return legalState;
    }

    public void setLegalState(String legalState) {
        this.legalState = legalState;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public double getPlanetRadius() {
        return planetRadius;
    }

    public void setPlanetRadius(double planetRadius) {
        this.planetRadius = planetRadius;
    }
}
