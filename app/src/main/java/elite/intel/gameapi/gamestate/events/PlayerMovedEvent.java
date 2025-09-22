package elite.intel.gameapi.gamestate.events;

public class PlayerMovedEvent {

    private double latitude, longitude, planetRadius;

    public PlayerMovedEvent(double latitude, double longitude, double planetRadius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.planetRadius = planetRadius;
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
}
