package elite.companion.model;

import com.google.gson.annotations.SerializedName;

public class MiningRefinedDTO {

    @SerializedName("mineralType")
    private String mineralType;
    private String timestamp;

    public String getMineralType() {
        return mineralType;
    }

    public void setMineralType(String mineralType) {
        this.mineralType = mineralType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
