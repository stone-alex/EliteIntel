package elite.companion.model;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.util.Map;

public class ProspectedAsteroidDTO extends BaseEventDTO {

    @SerializedName("Materials")
    public Map<String, Float> Materials; // e.g., {"Tritium": 0.29}
    public String matchingDroneId;

    public ProspectedAsteroidDTO(String timestamp, Map<String, Float> materials) {
        super(timestamp, 2, Duration.ofMinutes(10)); // Medium priority, short TTL
        this.Materials = materials;
    }

    @Override
    public String toString() {
        return timestamp + ": Asteroid with " + Materials;
    }
}
