package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.NavigateToNextTradeRouteStopHandler;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.search.spansh.traderoute.TradeCommodity;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.ai.brain.handlers.query.Queries.TARGET_MARKET_STATION_NAME;

public class RemindTargetMarketStationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        DestinationReminderManager destinationReminder = DestinationReminderManager.getInstance();
        NavigateToNextTradeRouteStopHandler.Reminder data = GsonFactory.getGson().fromJson(destinationReminder.getDestinationAsJson(), NavigateToNextTradeRouteStopHandler.Reminder.class);

        String pickupAtStation = data.stopInfo().getSourceStation();
        String dropOffAtStation = data.stopInfo().getDestinationStation();

        List<TradeCommodity> commodities = data.commodities();
        List<Commodity> list = new ArrayList<>();
        for (TradeCommodity commodity : commodities) {
            String name = commodity.getName();
            int amount = commodity.getAmount();
            long buyPrice = commodity.getSourceCommodity().getBuyPrice();
            list.add(new Commodity(name, amount, buyPrice));
        }

        return process(
                new AiDataStruct(
                        TARGET_MARKET_STATION_NAME.getInstructions(),
                        new DataDto(pickupAtStation, dropOffAtStation, list)
                ),
                originalUserInput
        );
    }

    record Commodity(String name, int amount, long buyPrice) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }


    record DataDto(String pickupAtStation, String dropOffAtStation, List<Commodity> commodities) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
