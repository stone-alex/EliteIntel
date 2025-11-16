package elite.intel.ai.search.spansh.station;

import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class DestinationDto implements ToJsonConvertible {

    @SerializedName("body_name")
    private String bodyName;

    @SerializedName("body_gravity")
    private double bodyGravity;

    @SerializedName("distance_to_arrival")
    private double distanceToArrival;

    @SerializedName("has_large_pad")
    private boolean hasLargePad;

    @SerializedName("is_planetary")
    private boolean isPlanetary;

    @SerializedName("large_pads")
    private int largePads;

    @SerializedName("medium_pads")
    private int mediumPads;

    @SerializedName("name")
    private String stationName;

    @SerializedName("small_pads")
    private int smallPads;

    @SerializedName("system_power")
    private List<String> systemPower;


    public String getBodyName() {
        return bodyName;
    }

    public double getBodyGravity() {
        return bodyGravity;
    }

    public double getDistanceToArrival() {
        return distanceToArrival;
    }

    public boolean isHasLargePad() {
        return hasLargePad;
    }

    public boolean isPlanetary() {
        return isPlanetary;
    }

    public int getLargePads() {
        return largePads;
    }

    public int getMediumPads() {
        return mediumPads;
    }

    public String getStationName() {
        return stationName;
    }

    public int getSmallPads() {
        return smallPads;
    }

    public List<String> getSystemPower() {
        return systemPower;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
