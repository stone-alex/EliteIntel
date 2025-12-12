package elite.intel.search.eddn.mappers;

import elite.intel.search.eddn.schemas.OutfittingMessage;
import elite.intel.gameapi.gamestate.dtos.GameEvents;

public class OutfittingMapper {

    public static OutfittingMessage map(GameEvents.OutfittingEvent event) {
        OutfittingMessage msg = new OutfittingMessage();
        msg.setMarketId(event.getMarketID());
        msg.setStationName(event.getStationName());
        msg.setSystemName(event.getStarSystem());
        msg.setHorizons(event.isHorizons());
        msg.setModules(event.getItems());
        msg.setTimestamp(event.getTimestamp());
        return msg;
    }
}
