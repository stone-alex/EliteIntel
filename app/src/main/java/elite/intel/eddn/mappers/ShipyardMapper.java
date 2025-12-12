package elite.intel.eddn.mappers;

import elite.intel.eddn.schemas.ShipyardMessage;
import elite.intel.gameapi.gamestate.dtos.GameEvents;

import java.util.*;

public class ShipyardMapper {

    public static ShipyardMessage map(GameEvents.ShipyardEvent event) {
        List<GameEvents.ShipyardEvent.ShipPrice> priceList = event.getPriceList();
        Set<String> ships = new HashSet<>();
        for (GameEvents.ShipyardEvent.ShipPrice price : priceList) {
            ships.add(price.getShipType());
        }

        ShipyardMessage message = new ShipyardMessage();
        message.setMarketId(event.getMarketID());
        message.setStationName(event.getStationName());
        message.setSystemName(event.getStarSystem());
        message.setShips(ships.stream().toList());
        message.setTimestamp(event.getTimestamp());
        message.setStationName(event.getStationName());
        message.setSystemName(event.getStarSystem());
        return message;
    }
}
