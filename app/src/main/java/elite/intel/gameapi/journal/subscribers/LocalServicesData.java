package elite.intel.gameapi.journal.subscribers;

import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.MarketDto;
import elite.intel.ai.search.edsm.dto.OutfittingDto;
import elite.intel.ai.search.edsm.dto.ShipyardDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class LocalServicesData {

    public static String setLocalServicesData(long marketId) {
        StringBuilder sb = new StringBuilder();
        MarketDto marketDto = EdsmApiClient.searchMarket(marketId, null, null);
        OutfittingDto outfittingDto = EdsmApiClient.searchOutfitting(marketId, null, null);
        ShipyardDto shipyardDto = EdsmApiClient.searchShipyard(marketId, null, null);

        PlayerSession playerSession = PlayerSession.getInstance();

        if (marketDto.getData() != null && marketDto.getData().getCommodities() != null) {
            sb.append(" Market, ");
            playerSession.getCurrentLocation().setMarket(marketDto);
        }

        if (outfittingDto.getData() != null && outfittingDto.getData().getOutfitting() != null) {
            sb.append(" Outfitting, ");
            playerSession.getCurrentLocation().setOutfitting(outfittingDto);
        }

        if (shipyardDto.getData() != null && shipyardDto.getData().getShips() != null) {
            sb.append(" Shipyard, ");
            playerSession.getCurrentLocation().setShipyard(shipyardDto);
        }
        playerSession.save();
        return sb.toString().trim();
    }

    public static void clearLocalServicesData() {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.setMarket(null);
        currentLocation.setOutfitting(null);
        currentLocation.setShipyard(null);
        playerSession.saveCurrentLocation(currentLocation);
    }
}
