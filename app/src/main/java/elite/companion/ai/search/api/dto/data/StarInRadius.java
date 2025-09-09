package elite.companion.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

public class StarInRadius {
    @SerializedName("distance")
    public double distance;
    @SerializedName("name")
    public String name;
    @SerializedName("id")
    public int id;
    @SerializedName("coords")
    public StarSystemCoordinates coords;

    public double getDistance() {
        return distance;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public StarSystemCoordinates getCoords() {
        return coords;
    }
}
