package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.commons.TradersAndBrokersSearch;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.traderandbroker.TraderType;
import elite.intel.gameapi.EventBusManager;

public class FindRawMaterialTraderHandler extends CommandOperator implements CommandHandler {


    private final GameController gameController;

    public FindRawMaterialTraderHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.gameController = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        EventBusManager.publish(new AiVoxResponseEvent("Searching for " + TraderType.RAW.getType() + " material traders... Stand by..."));
        TradersAndBrokersSearch search = TradersAndBrokersSearch.getInstance();
        RoutePlotter routePlotter = new RoutePlotter(this.gameController);
        routePlotter.plotRoute(search.location(TraderType.RAW, null, 250));
    }
}
