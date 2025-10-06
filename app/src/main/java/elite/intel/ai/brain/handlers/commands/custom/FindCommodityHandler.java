package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.market.MarketSearchCriteria;
import elite.intel.ai.search.spansh.market.SpanshMarketClient;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.ai.search.spansh.nearest.NearestKnownLocationSearch;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.io.IOException;
import java.util.List;

import static elite.intel.util.StringUtls.capitalizeWords;

public class FindCommodityHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = params.get("commodity");
        String commodity = capitalizeWords(jsonElement.getAsJsonPrimitive().getAsString().replace("\"", ""));

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearMarkets();

        LocationDto currentLocation = playerSession.getCurrentLocation();
        LocationDto nearestKnownLocation = NearestKnownLocationSearch.findNearest(
                currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()
        );

        String starName = nearestKnownLocation == null ? currentLocation.getStarName() : nearestKnownLocation.getStarName();

        SpanshMarketClient client = new SpanshMarketClient();
        try {
            List<StationMarket> markets = client.searchMarkets(new MarketSearchCriteria(
                    starName,
                    1,
                    250,
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
