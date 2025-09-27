package elite.intel.gameapi.journal.events.dto;

public class TargetLocation {

    double latitude;
    double longitude;
    boolean isEnabled;
    long requestedTime;

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
}
