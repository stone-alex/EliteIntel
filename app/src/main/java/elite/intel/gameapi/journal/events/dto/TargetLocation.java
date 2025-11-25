package elite.intel.gameapi.journal.events.dto;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class TargetLocation implements ToJsonConvertible {

    double latitude;
    double longitude;
    boolean isEnabled;
    long requestedTime;

    public TargetLocation() {
        requestedTime = System.currentTimeMillis();
    }

    public TargetLocation(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public long getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(long requestedTime) {
        this.requestedTime = requestedTime;
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TargetLocation that = (TargetLocation) o;
        return Double.compare(getLatitude(), that.getLatitude()) == 0 && Double.compare(getLongitude(), that.getLongitude()) == 0 && isEnabled() == that.isEnabled() && getRequestedTime() == that.getRequestedTime();
    }

    @Override public int hashCode() {
        int result = Double.hashCode(getLatitude());
        result = 31 * result + Double.hashCode(getLongitude());
        result = 31 * result + Boolean.hashCode(isEnabled());
        result = 31 * result + Long.hashCode(getRequestedTime());
        return result;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
