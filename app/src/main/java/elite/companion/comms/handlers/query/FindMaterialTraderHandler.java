package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;

public class FindMaterialTraderHandler implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        return GenericResponse.getInstance().genericResponse("Query command is present, but not implemented yet. ");
    }
}
