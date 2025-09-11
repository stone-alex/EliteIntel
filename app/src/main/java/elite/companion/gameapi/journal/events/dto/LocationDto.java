package elite.companion.gameapi.journal.events.dto;

import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

public class LocationDto implements ToJsonConvertible {

    private double X;
    private double Y;
    private double Z;

    private String starName;
    private String allegiance;
    private String security;
    private String government;

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

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    public void setAllegiance(String allegiance) {
        this.allegiance = allegiance;
    }

    public String getAllegiance() {
        return allegiance;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getSecurity() {
        return security;
    }

    public void setGovernment(String government) {
        this.government = government;
    }

    public String getGovernment() {
        return government;
    }
}
