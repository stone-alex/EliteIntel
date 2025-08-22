package elite.companion.model;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

public class ScanDTO extends BaseEventDTO {

    private String TargetType; // e.g., "Ship"
    private boolean isWanted;
    private boolean isMissionTarget;
    private String contactId; // Custom: Unique ID from journal (or timestamp)

    public ScanDTO(String timestamp, String targetType, boolean isWanted, boolean isMissionTarget, String contactId) {
        super(timestamp, 1, Duration.ofSeconds(30)); // High priority, short TTL
        this.TargetType = targetType;
        this.isWanted = isWanted;
        this.isMissionTarget = isMissionTarget;
        this.contactId = contactId;
    }

    public String getTargetType() {
        return TargetType;
    }

    public void setTargetType(String targetType) {
        TargetType = targetType;
    }

    public boolean isWanted() {
        return isWanted;
    }

    public void setWanted(boolean wanted) {
        isWanted = wanted;
    }

    public boolean isMissionTarget() {
        return isMissionTarget;
    }

    public void setMissionTarget(boolean missionTarget) {
        isMissionTarget = missionTarget;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ScanDTO scanDTO = (ScanDTO) o;
        return isWanted() == scanDTO.isWanted() && isMissionTarget() == scanDTO.isMissionTarget() && Objects.equals(getTargetType(), scanDTO.getTargetType()) && Objects.equals(getContactId(), scanDTO.getContactId());
    }

    @Override public int hashCode() {
        int result = Objects.hashCode(getTargetType());
        result = 31 * result + Boolean.hashCode(isWanted());
        result = 31 * result + Boolean.hashCode(isMissionTarget());
        result = 31 * result + Objects.hashCode(getContactId());
        return result;
    }

    @Override public String toString() {
        return new StringJoiner(", ", ScanDTO.class.getSimpleName() + "[", "]")
                .add("TargetType='" + TargetType + "'")
                .add("isWanted=" + isWanted)
                .add("isMissionTarget=" + isMissionTarget)
                .add("contactId='" + contactId + "'")
                .toString();
    }
}
