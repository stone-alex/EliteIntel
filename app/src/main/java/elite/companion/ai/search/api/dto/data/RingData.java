package elite.companion.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

public class RingData {
    @SerializedName("name")
    public String name;
    @SerializedName("type")
    public String type;
    @SerializedName("mass")
    public long mass;
    @SerializedName("innerRadius")
    public double innerRadius;
    @SerializedName("outerRadius")
    public double outerRadius;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getMass() {
        return mass;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public double getOuterRadius() {
        return outerRadius;
    }
}
