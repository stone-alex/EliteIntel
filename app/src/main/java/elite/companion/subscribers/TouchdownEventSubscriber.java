package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.TouchdownEvent;

public class TouchdownEventSubscriber {

    public TouchdownEventSubscriber() {
        EventBusManager.register(this);
    }

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
        sb.append("Star System: ");
        sb.append(starSystem);
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

    }
}
