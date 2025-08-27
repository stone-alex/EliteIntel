package elite.companion.gameapi.gamestate.events;

import com.google.gson.Gson;

public class NavRouteDto extends BaseJsonDto {

    private int leg;
    private int remainingJumps;
    private String starClass;
    private String name;
    private boolean isScoopable;


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
        return new Gson().toJson(this);
    }
}
