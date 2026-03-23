package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.util.ClipboardUtils;

public class PasteFromMemoryHandler extends CommandOperator implements CommandHandler {

    private final GameController controller;

    public PasteFromMemoryHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        String destination = ClipboardUtils.getClipboardText();
        RoutePlotter plotter = new RoutePlotter(controller);
        plotter.plotRoute(destination);
    }
}
