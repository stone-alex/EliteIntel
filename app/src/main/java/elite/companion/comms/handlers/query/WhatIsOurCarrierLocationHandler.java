package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

public class WhatIsOurCarrierLocationHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        String carrierLocation = String.valueOf(SystemSession.getInstance().get(SystemSession.CARRIER_LOCATION));
        String playerRank = String.valueOf(PlayerSession.getInstance().get(PlayerSession.PLAYER_HIGHEST_MILITARY_RANK));
        return GenericResponse.getInstance().genericResponse("Carrier is located in" + carrierLocation + ", " + playerRank);
    }
}
