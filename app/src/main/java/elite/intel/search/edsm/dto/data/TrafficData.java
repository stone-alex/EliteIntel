package elite.intel.search.edsm.dto.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class TrafficData {
    @SerializedName("id")
    public int id;
    @SerializedName("id64")
    public long id64;
    @SerializedName("name")
    public String name;
    /*
        @SerializedName("url")
        public String url;
    */
    @SerializedName("discovery")
    public DiscoveryData discovery;
    @SerializedName("traffic")
    public TrafficStats traffic;
    @SerializedName("breakdown")
    public Map<String, Integer> breakdown;

    public int getId() {
        return id;
    }

    public long getId64() {
        return id64;
    }

    public String getName() {
        return name;
    }

/*
    public String getUrl() {
        return url;
    }
*/

    public DiscoveryData getDiscovery() {
        return discovery;
    }

    public TrafficStats getTraffic() {
        return traffic == null ? new TrafficStats() : traffic;
    }

    public Map<String, Integer> getBreakdown() {
        return breakdown;
    }
}
