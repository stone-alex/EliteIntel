package elite.companion.model;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

public class LaunchDroneDTO extends BaseEventDTO {
    private String Type; // e.g., "Prospector"
    private String droneId; // Custom: Use timestamp as unique ID

    public LaunchDroneDTO(String timestamp, String type) {
        super(timestamp, 3, Duration.ofMinutes(5)); // Low priority, short TTL
        this.Type = type;
        this.droneId = timestamp; // Custom field for tracking
    }


    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        LaunchDroneDTO that = (LaunchDroneDTO) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getDroneId(), that.getDroneId());
    }

    @Override public int hashCode() {
        int result = Objects.hashCode(getType());
        result = 31 * result + Objects.hashCode(getDroneId());
        return result;
    }

    @Override public String toString() {
        return new StringJoiner(", ", LaunchDroneDTO.class.getSimpleName() + "[", "]")
                .add("timestamp='" + timestamp + "'")
                .add("priority=" + priority)
                .add("endOfLife=" + endOfLife)
                .add("isProcessed=" + isProcessed)
                .toString();
    }
}
