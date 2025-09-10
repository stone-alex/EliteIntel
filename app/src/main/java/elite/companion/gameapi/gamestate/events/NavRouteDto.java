package elite.companion.gameapi.gamestate.events;

import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

public class NavRouteDto extends BaseJsonDto implements ToJsonConvertible {

    private int leg;
    private int remainingJumps;
    private String starClass;
    private String name;
    private boolean isScoopable;
    float X;
    float  Y;
    float  Z;


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

    public float getX() {
        return X;
    }

    public void setX(float x) {
        X = x;
    }

    public float getY() {
        return Y;
    }

    public void setY(float y) {
        Y = y;
    }

    public float getZ() {
        return Z;
    }

    public void setZ(float z) {
        Z = z;
    }
}
