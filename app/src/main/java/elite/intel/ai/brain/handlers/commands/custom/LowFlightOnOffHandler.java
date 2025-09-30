package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.session.PlayerSession;

public class LowFlightOnOffHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String lowFlight = String.valueOf(playerSession.get(PlayerSession.LOW_ALTITUDE_FLIGHT));

        if("true".equalsIgnoreCase(lowFlight)){
            playerSession.put(PlayerSession.LOW_ALTITUDE_FLIGHT, false);
        } else {
            playerSession.put(PlayerSession.LOW_ALTITUDE_FLIGHT, true);
        }
    }
}
