package elite.intel.gameapi.journal.subscribers;

import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.MarketDto;
import elite.intel.search.edsm.dto.OutfittingDto;
import elite.intel.search.edsm.dto.ShipyardDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class LocalServicesData {

    public static String setLocalServicesData(long marketId) {
        StringBuilder sb = new StringBuilder();
        MarketDto marketDto = EdsmApiClient.searchMarket(marketId, null, null, 0);
        OutfittingDto outfittingDto = EdsmApiClient.searchOutfitting(marketId, null, null);
        ShipyardDto shipyardDto = EdsmApiClient.searchShipyard(marketId, null, null);

        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (marketDto.getData() != null && marketDto.getData().getCommodities() != null) {
            sb.append(" Market ");
            sb.append(marketDto.getData().getName());
            sb.append(", ");
            currentLocation.setMarket(marketDto);
        }

        if (outfittingDto.getData() != null && outfittingDto.getData().getOutfitting() != null) {
            sb.append(" Outfitting, ");
            currentLocation.setOutfitting(outfittingDto);
        }

        if (shipyardDto.getData() != null && shipyardDto.getData().getShips() != null) {
            sb.append(" Shipyard, ");
            currentLocation.setShipyard(shipyardDto);
        }
        playerSession.saveLocation(currentLocation);
        return sb.toString().trim();
    }
}
