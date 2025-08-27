package elite.companion.comms.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.session.SystemSession;

import static elite.companion.session.SystemSession.CURRENT_SYSTEM;

public class QueryCurrentSystemHandler implements QueryHandler {

    @Override
    public String handle(JsonObject params) {
        SystemSession systemSession = SystemSession.getInstance();
        Object object = systemSession.getObject(CURRENT_SYSTEM);
        String currentSystemName = object == null ? "unknown" : String.valueOf(object);
        String result = "Data for: "+currentSystemName+", "+systemSession.getSignals();
        System.out.println(result);
        return result;
    }
}
