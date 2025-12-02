package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.market.MarketSearchCriteria;
import elite.intel.search.spansh.market.SpanshMarketClient;
import elite.intel.search.spansh.market.StationMarketDto;
import elite.intel.session.PlayerSession;

import java.io.IOException;
import java.util.List;

import static elite.intel.util.StringUtls.capitalizeWords;
import static elite.intel.util.StringUtls.fuzzyCommodityMatch;

public class FindCommodityHandler extends CommandOperator implements CommandHandler {

    private GameController commandHandler;

    public FindCommodityHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        EventBusManager.publish(new AiVoxResponseEvent("Searching for commodities..."));
        String commodity =
                capitalizeWords(
                        fuzzyCommodityMatch(
                                params.get("key").getAsString(), 1
                        )
                );

        if (commodity == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, I couldn't find any commodities matching." + params.get("key").getAsString()));
            return;
        }


        PlayerSession playerSession = PlayerSession.getInstance();
        String starName = playerSession.getPrimaryStarName();

        SpanshMarketClient client = new SpanshMarketClient();
        try {
            List<StationMarketDto> markets = client.searchMarkets(new MarketSearchCriteria(
                    starName,
                    1,
                    1000,
                    commodity,
                    true,
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
                DestinationReminderManager reminderManager = DestinationReminderManager.getInstance();
                reminderManager.setDestination(stationMarketDto.toJson());
                plotter.plotRoute(stationMarketDto.systemName());
            }

        } catch (IOException | InterruptedException e) {
            EventBusManager.publish(new AiVoxResponseEvent("Unable to find commodity: " + commodity + "."));
        }
    }
}
