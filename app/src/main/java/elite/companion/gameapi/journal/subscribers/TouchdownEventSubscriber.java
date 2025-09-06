package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.TouchdownEvent;

@SuppressWarnings("unused")
public class TouchdownEventSubscriber {

    @Subscribe
    public void onTouchdownEvent(TouchdownEvent event) {
        String body = event.getBody();
        float latitude = event.getLatitude();
        float longitude = event.getLongitude();
        String pointOfInterest = event.getNearestDestinationLocalised();
        String starSystem = event.getStarSystem();
        boolean isStation = event.isOnStation();
        boolean isOnPlanet = event.isOnPlanet();
        String locationType = isStation ? "Station" : isOnPlanet ? "Planet" : null;

        StringBuilder sb = new StringBuilder();
        sb.append("Touchdown: ");
        sb.append(" ");
        sb.append(event.isPlayerControlled() ? "Manual" : "Unmanned");
        sb.append(" ");
        if (isStation) sb.append(" On Station ");
        if (isStation) sb.append(" On ").append(event.getBody()).append(". ").append("Nearest Destination: ").append(pointOfInterest).append(".");

        sb.append(" ");
        sb.append(locationType == null ? "Unknown" : locationType);
        sb.append(" ");
        sb.append(body);
        sb.append(" ");
        sb.append("Latitude: ");
        sb.append(latitude);
        sb.append(" ");
        sb.append("Longitude: ");
        sb.append(longitude);
        sb.append(" ");
        sb.append("Point of Interest: ");
        sb.append(pointOfInterest);

        EventBusManager.publish(new SensorDataEvent(sb.toString()));

    }
}
