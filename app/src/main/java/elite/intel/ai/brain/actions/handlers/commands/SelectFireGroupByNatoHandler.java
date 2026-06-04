package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.gameapi.FireGroups;

import static elite.intel.gameapi.FireGroups.fireGroupByNato;

public class SelectFireGroupByNatoHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        JsonElement key = params.get("key");
        if (key == null) {
            return;
        }

        String nato = key.getAsString();
        if (nato == null) {
            return;
        }

        int fireGroupInSettings = fireGroupByNato(nato);
        if (fireGroupInSettings == -1) return;

        FireGroups.cycleToGroup(fireGroupInSettings);
    }
}
