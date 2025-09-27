package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

import static elite.intel.util.NavigationUtils.getHeading;

public class GetHeadingToLandingZone implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        double latitude = playerSession.getStatus().getLatitude();
        double longitude = playerSession.getStatus().getLongitude();
        double planetRadius = playerSession.getStatus().getPlanetRadius();

        LocationDto currentLocation = playerSession.getCurrentLocation();
        double[] landingCoordinates = currentLocation.getLandingCoordinates();

        TargetLocation targetLocation = new TargetLocation();
        targetLocation.setLatitude(currentLocation.getLandingCoordinates()[0]);
        targetLocation.setLongitude(currentLocation.getLandingCoordinates()[1]);
        playerSession.setTracking(targetLocation);

        String heading = getHeading(landingCoordinates[0], landingCoordinates[1], latitude, longitude, planetRadius);

        EventBusManager.publish(new VoiceProcessEvent(heading));
    }

}
