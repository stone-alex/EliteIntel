package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;

/**
 * The WhatIsOurCarrierLocationHandler class is responsible for handling user queries
 * related to the current location of the carrier, as well as the player's highest military rank.
 * <p>
 * This class extends the BaseQueryAnalyzer to leverage its core query analysis capabilities
 * and implements QueryHandler to ensure it conforms to the standard query handling contract.
 * <p>
 * Responsibilities:
 * - Processes the user query to retrieve the carrier's location via session data.
 * - Retrieves the player's highest military rank from the session data.
 * - Constructs a response combining carrier location and military rank details.
 * <p>
 * Methods:
 * - handle: Handles the specific "what is our carrier location" action, processes the input parameters,
 * and retrieves the relevant details from the player session to build the response.
 * <p>
 * Exceptions:
 * This class can throw general exceptions during the execution of the handle method if
 * there are issues accessing session data, processing the input, or constructing the response.
 */
public class WhatIsOurCarrierLocationHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        String carrierLocation = playerSession.getCarrierData() == null? "unknown ": playerSession.getCarrierData().getLocation();
        String playerRank = playerSession.getPlayerHighestMilitaryRank();
        return GenericResponse.getInstance().genericResponse("Carrier is located in" + carrierLocation + ", " + playerRank);
    }
}
