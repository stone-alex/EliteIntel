package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.comms.VoiceGenerator;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

public class AccessShipDataHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.updateSession(SystemSession.SHIP_DATA, playerSession.getSummary());
        VoiceGenerator.getInstance().speak(responseText);
    }
}
