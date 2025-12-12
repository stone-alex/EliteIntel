package elite.intel.eddn.mappers;

import elite.intel.eddn.schemas.CommodityMessage;
import elite.intel.gameapi.gamestate.dtos.GameEvents.MarketEvent;

import java.util.stream.Collectors;

public class MarketMapper {

    public static CommodityMessage map(MarketEvent event) {
        CommodityMessage msg = new CommodityMessage();
        msg.setTimestamp(event.getTimestamp());
        msg.setSystemName(event.getStarSystem());
        msg.setStationName(event.getStationName());
        msg.setMarketId(event.getMarketID());
        msg.setCommodities(event.getItems().stream().map(item -> {
            CommodityMessage.CommodityItem ci = new CommodityMessage.CommodityItem();
            ci.setName(item.getName());
            ci.setBuyPrice(item.getBuyPrice());
            ci.setSellPrice(item.getSellPrice());
            ci.setMeanPrice(item.getMeanPrice());
            ci.setStock(item.getStock());
            ci.setStockBracket(item.getStockBracket());
            ci.setDemand(item.getDemand());
            ci.setDemandBracket(item.getDemandBracket());
            return ci;
        }).collect(Collectors.toList()));
        return msg;
    }
}