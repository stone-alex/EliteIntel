package elite.companion.gameapi.journal.subscribers;

import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.dto.MarketDto;
import elite.companion.ai.search.api.dto.OutfittingDto;
import elite.companion.ai.search.api.dto.ShipyardDto;
import elite.companion.session.PlayerSession;

public class LocalServicesData {

    public static String setLocalServicesData(long marketId) {
        StringBuilder sb = new StringBuilder();
        MarketDto marketDto = EdsmApiClient.searchMarket(marketId, null, null);
        OutfittingDto outfittingDto = EdsmApiClient.searchOutfitting(marketId, null, null);
        ShipyardDto shipyardDto = EdsmApiClient.searchShipyard(marketId, null, null);

        PlayerSession playerSession = PlayerSession.getInstance();

        if (marketDto.getData() != null && marketDto.getData().getCommodities() != null) {
            sb.append(" Market, ");
            playerSession.put(PlayerSession.LOCAL_MARKET_JSON, marketDto.toJson());
        }

        if (outfittingDto.getData() != null && outfittingDto.getData().getOutfitting() != null) {
            sb.append(" Outfitting, ");
            playerSession.put(PlayerSession.LOCAL_OUTFITING_JSON, outfittingDto.toJson());
        }

        if (shipyardDto.getData() != null && shipyardDto.getData().getShips() != null) {
            sb.append(" Shipyard, ");
            playerSession.put(PlayerSession.LOCAL_SHIP_YARD_JSON, shipyardDto.toJson());
        }
        return sb.toString().trim();
    }

    public static void clearLocalServicesData() {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.remove(PlayerSession.LOCAL_MARKET_JSON);
        playerSession.remove(PlayerSession.LOCAL_OUTFITING_JSON);
        playerSession.remove(PlayerSession.LOCAL_SHIP_YARD_JSON);
    }
}
