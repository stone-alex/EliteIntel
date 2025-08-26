package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.session.SystemSession;

import static elite.companion.session.SystemSession.CURRENT_SYSTEM;

public class GetCurrentSystemHandler implements QueryHandler {

    @Override
    public String handle(JsonObject params) {
        SystemSession session = SystemSession.getInstance();
        String currentSystem = session.getSessionValue(CURRENT_SYSTEM, String.class); // e.g., from last Location/FSDJump event
        if (currentSystem == null) {
            return "No current system set in SystemSession for current location";
        }
        return currentSystem;
    }
}
