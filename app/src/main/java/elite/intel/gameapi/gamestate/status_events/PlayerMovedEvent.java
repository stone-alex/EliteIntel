package elite.intel.gameapi.gamestate.status_events;

public class PlayerMovedEvent {

    private double latitude, longitude, planetRadius, altitude;

    public PlayerMovedEvent(double latitude, double longitude, double planetRadius, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.planetRadius = planetRadius;
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getPlanetRadius() {
        return planetRadius;
    }

    public double getAltitude() {
        return altitude;
    }
}
