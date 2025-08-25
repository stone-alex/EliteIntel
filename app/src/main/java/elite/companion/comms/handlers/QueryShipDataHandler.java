package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.session.SystemSession;

public class QueryShipDataHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        SystemSession systemSession = SystemSession.getInstance();

    }
}
