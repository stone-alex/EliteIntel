package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;

public class RadarAnnouncementOnOffHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        boolean isOn = params.get("state").getAsBoolean();
        PlayerSession.getInstance().setRadarContactAnnouncementOn(isOn);
    }
}
