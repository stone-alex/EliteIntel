package elite.companion.gameapi.journal.events.dto;

import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

public class LocationDto implements ToJsonConvertible {

    private double X;
    private double Y;
    private double Z;

    private String starName;

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
}
