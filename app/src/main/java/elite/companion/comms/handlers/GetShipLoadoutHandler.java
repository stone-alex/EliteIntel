package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.session.SystemSession;

public class GetShipLoadoutHandler implements QueryHandler {

    @Override public String handle(JsonObject params) throws Exception {
        String result = SystemSession.getInstance().getSessionValue(SystemSession.SHIP_LOADOUT_JSON, String.class);
        if (result == null) {
            return "No ship loadout found in SystemSession";
        }
        return result;
    }
}
