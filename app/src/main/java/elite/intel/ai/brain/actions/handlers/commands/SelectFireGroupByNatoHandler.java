package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.gameapi.FireGroups;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_CYCLE_NEXT_FIRE_GROUP;
import static elite.intel.gameapi.FireGroups.fireGroupByNato;

public class SelectFireGroupByNatoHandler implements CommandHandler {

    private final Status status = Status.getInstance();

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

        int counter = 0;
        while (fireGroupInSettings != status.getFireGroup()) {
            GameControllerBus.publish(GameInputSequenceEvent.of(
                    GameInputStep.bindingTap(BINDING_CYCLE_NEXT_FIRE_GROUP.getGameBinding()),
                    GameInputStep.delay(300)
            ));
            counter++;
            if (counter > FireGroups.fireGroups.size()) break;
        }
    }
}
