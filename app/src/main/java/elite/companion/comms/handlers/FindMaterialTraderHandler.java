package elite.companion.comms.handlers;

import com.google.gson.JsonObject;

public class FindMaterialTraderHandler implements QueryHandler {

    @Override public String handle(JsonObject params) throws Exception {
        //InaraApiClient.getInstance().findMaterialTrader(params.get("material").getAsString());

        return "";
    }
}
