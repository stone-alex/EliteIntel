package elite.intel.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

public class ControllingFaction {
    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
