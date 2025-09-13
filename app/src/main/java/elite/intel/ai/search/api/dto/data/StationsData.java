package elite.intel.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StationsData {
    @SerializedName("id")
    public int id;
    @SerializedName("id64")
    public long id64;
    @SerializedName("name")
    public String name;
    @SerializedName("url")
    public String url;
    @SerializedName("stations")
    public List<Station> stations;

    public int getId() {
        return id;
    }

    public long getId64() {
        return id64;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<Station> getStations() {
        return stations;
    }
}
