package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.db.managers.SubSystemsManager;

import java.util.Locale;

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
            String keyAsString = key
                    .getAsString()
                    .toLowerCase(Locale.ROOT)
                    .replace(".", "")
                    .replace("frame shift drive", "fsd")
                    .replace("drive", "fsd")
                    .replace(",", "");

            if ("frame shift drive".equalsIgnoreCase(keyAsString)) {
                subSystem = "FSD";
            } else if ("drive".equalsIgnoreCase(keyAsString)) {
                subSystem = "FSD";
            } else {
                subSystem = keyAsString;
            }
        }
        SubSystemsManager.getInstance(controller).targetSubSystem(subSystem);
    }
}
