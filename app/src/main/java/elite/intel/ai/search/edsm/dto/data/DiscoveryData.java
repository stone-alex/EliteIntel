package elite.intel.ai.search.edsm.dto.data;

import com.google.gson.annotations.SerializedName;

public class DiscoveryData {
    @SerializedName("commander")
    public String commander;
    @SerializedName("date")
    public String date;

    public String getCommander() {
        return commander;
    }

    public String getDate() {
        return date;
    }
}
