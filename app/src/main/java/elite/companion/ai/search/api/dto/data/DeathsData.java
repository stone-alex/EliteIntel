package elite.companion.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

public class DeathsData {
    @SerializedName("id")
    public int id;
    @SerializedName("id64")
    public long id64;
    @SerializedName("name")
    public String name;
    @SerializedName("url")
    public String url;
    @SerializedName("deaths")
    public DeathsStats deaths;

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

    public DeathsStats getDeaths() {
        return deaths;
    }
}
