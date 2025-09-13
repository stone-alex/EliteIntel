package elite.intel.about.dto;


import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;

public class AINameDto {
    @SerializedName("name")
    private final String name;
    @SerializedName("description")
    private final String description;

    public AINameDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}