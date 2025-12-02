package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.search.spansh.market.MarketSearchCriteria;
import elite.intel.search.spansh.market.SpanshMarketClient;
import elite.intel.search.spansh.market.StationMarketDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.StringUtls;

import java.io.IOException;
import java.util.List;

public class FindCommodityHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        String commodity = StringUtls.capitalizeWords(params.get("key").getAsString());
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearMarkets();

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
                playerSession.setMarkets(markets);
            }
            EventBusManager.publish(
                    new SensorDataEvent(
                            "Found " + numMarkets + " market(s)"
                                    + (numMarkets == 1 ? "" : "s") + " for " + commodity
                                    + (numMarkets > 0 ? ". Prompt user to ask you to plot a route to the market with best price." : "")
                    )
            );

        } catch (IOException | InterruptedException e) {
            EventBusManager.publish(new AiVoxResponseEvent("Unable to find commodity: " + commodity + "."));
        }
    }
}
