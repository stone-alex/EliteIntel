package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.MissionCompletedEvent;
import elite.intel.gameapi.journal.events.TouchdownEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

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

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if(currentLocation != null) {
            // only set LZ coordinates on touchdown. Event if ship departs, we can go back to where it can land again.
            currentLocation.setLandingCoordinates(new double[]{ latitude, longitude});
            playerSession.saveLocation(currentLocation);
        }

        if (pointOfInterest != null && !pointOfInterest.isEmpty()) {
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Touchdown!"));
        }
    }
}
