package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NavigateToCoordinatesHandler implements CommandHandler {

    private static final Logger log = LogManager.getLogger(NavigateToCoordinatesHandler.class);

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();

        if(params.get("lat") == null || params.get("lon") == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Say Again?"));
            return;
        }

        double latitude = params.get("lat").getAsDouble();
        double longitude = params.get("lon").getAsDouble();

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            log.error("Invalid coordinates: " + latitude + ", " + longitude);
            EventBusManager.publish(new AiVoxResponseEvent("Coordinates must be between -90 and 90 and -180 and 180"));
        } else {
            TargetLocation tracking = playerSession.getTracking();
            tracking.setEnabled(true);
            tracking.setLatitude(latitude);
            tracking.setLongitude(longitude);
            tracking.setRequestedTime(System.currentTimeMillis());
            playerSession.setTracking(tracking);
            log.info("Starting navigation to coordinates: " + latitude + ", " + longitude);
            EventBusManager.publish(new AiVoxResponseEvent("Starting navigation to latitude: " + latitude + ", longitude: " + longitude + "."));
        }
    }
}
