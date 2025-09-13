package elite.intel.gameapi.gamestate.events;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class NavRouteDto extends BaseJsonDto implements ToJsonConvertible {

    private int leg;
    private int remainingJumps;
    private String starClass;
    private String name;
    private boolean isScoopable;
    double X;
    double  Y;
    double  Z;


    public int getLeg() {
        return leg;
    }

    public void setLeg(int leg) {
        this.leg = leg;
    }

    public String getStarClass() {
        return starClass;
    }

    public void setStarClass(String starClass) {
        this.starClass = starClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isScoopable() {
        return isScoopable;
    }

    public void setScoopable(boolean scoopable) {
        isScoopable = scoopable;
    }

    public int getRemainingJumps() {
        return remainingJumps;
    }

    public void setRemainingJumps(int remainingJumps) {
        this.remainingJumps = remainingJumps;
    }

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
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
}
