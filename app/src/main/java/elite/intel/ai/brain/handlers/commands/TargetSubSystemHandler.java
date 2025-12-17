package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.db.managers.SubSystemsManager;

public class TargetSubSystemHandler implements CommandHandler {

    private GameController controller;

    public TargetSubSystemHandler(GameController controller) {
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        JsonElement key = params.get("key");
        String subSystem;
        if (key == null) {
            subSystem = "power plant";
        } else {
            subSystem = key.getAsString();
        }
        SubSystemsManager.getInstance(controller).targetSubSystem(subSystem);
    }
}
