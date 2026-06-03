package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.db.managers.SubSystemsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public class TargetSubSystemHandler implements CommandHandler {

    private static final Logger log = LogManager.getLogger(TargetSubSystemHandler.class);

    @Override public void handle(String action, JsonObject params, String responseText) {
        log.debug("TargetSubSystemHandler received params: {}", params);
        JsonElement key = params.get("key");

        String subSystem;
        if (key == null) {
            log.debug("No 'key' param from LLM - defaulting to power plant");
            subSystem = "power plant";
        } else {
            subSystem = key.getAsString()
                    .toLowerCase(Locale.ROOT)
                    .replace(".", "")
                    .replace(",", "")
                    .replace("frame shift drive", "fsd")
                    .replace("thrusters", "drive")
                    .replace("engines", "drive")
                    .replace("shields", "shield generator")
                    .replace("powerplant", "power plant")
                    .trim();
            log.debug("LLM key=[{}] normalized to=[{}]", key.getAsString(), subSystem);
        }
        SubSystemsManager.getInstance().targetSubSystem(subSystem);
    }
}
