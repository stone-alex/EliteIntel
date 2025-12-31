package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.ShipDao;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.market.MarketSearchCriteria;
import elite.intel.search.spansh.market.SpanshMarketClient;
import elite.intel.search.spansh.market.StationMarketDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.ShipPadSizes;

import java.io.IOException;
import java.util.List;

import static elite.intel.util.StringUtls.capitalizeWords;
import static elite.intel.util.StringUtls.fuzzyCommodityMatch;

public class FindCommodityHandler extends CommandOperator implements CommandHandler {

    private GameController commandHandler;
    private PlayerSession playerSession = PlayerSession.getInstance();

    public FindCommodityHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        JsonElement key = params.get("key");
        if(key == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Please specify a commodity."));
            return;
        }
        String commodity =
                capitalizeWords(
                        fuzzyCommodityMatch(
                                key.getAsString(), 3
                        )
                );

        if (commodity == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, I couldn't find any commodities matching " + key.getAsString()));
            return;
        }

        int maxDistance = (int) (playerSession.getShipLoadout().getMaxJumpRange() * 5);
        EventBusManager.publish(new AiVoxResponseEvent("Searching markets with best price for " + commodity + " within " + maxDistance + " light years."));

        String starName = playerSession.getPrimaryStarName();
        SpanshMarketClient client = new SpanshMarketClient();
        final ShipDao.Ship ship = ShipManager.getInstance().getShip();
        try {
            boolean requireLargePad = "L".equals(ShipPadSizes.getPadSize(ship.getShipIdentifier()));
            List<StationMarketDto> markets = client.searchMarkets(new MarketSearchCriteria(
                    starName,
                    1,
                    maxDistance,
                    commodity,
                    false,
                    false,
                    true,
                    1,
                    true,
                    false
            ));


            int numMarkets = markets.size();
            if (numMarkets > 0) {
                RoutePlotter plotter = new RoutePlotter(this.commandHandler);
                StationMarketDto stationMarketDto = markets.stream().findFirst().get();

                if (requireLargePad && !stationMarketDto.hasLargePad()) {
                    EventBusManager.publish(new AiVoxResponseEvent("Warning. Station does not have a large pad."));
                }


                EventBusManager.publish(new AiVoxResponseEvent("Head to " + stationMarketDto.systemName() + " star system. " + stationMarketDto.stationName() + " port."));
                DestinationReminderManager reminderManager = DestinationReminderManager.getInstance();
                reminderManager.setDestination(stationMarketDto.toJson());
                plotter.plotRoute(stationMarketDto.systemName());
            } else {
                EventBusManager.publish(new AiVoxResponseEvent("Sorry, I couldn't find any markets that sell " + commodity + "."));
            }

        } catch (IOException | InterruptedException e) {
            EventBusManager.publish(new AiVoxResponseEvent("Unable to find commodity: " + commodity + "."));
        }
    }
}
