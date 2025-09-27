package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

public class NavigateToLandingZone implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        TargetLocation targetLocation = new TargetLocation();
        if(currentLocation.getLandingCoordinates() == null){
            EventBusManager.publish(new VoiceProcessEvent("Landing Zone Coordinates are not available"));
            return;
        }

        targetLocation.setLatitude(currentLocation.getLandingCoordinates()[0]);
        targetLocation.setLongitude(currentLocation.getLandingCoordinates()[1]);
        targetLocation.setEnabled(true);
        targetLocation.setRequestedTime(System.currentTimeMillis());
        playerSession.setTracking(targetLocation);
    }

}
