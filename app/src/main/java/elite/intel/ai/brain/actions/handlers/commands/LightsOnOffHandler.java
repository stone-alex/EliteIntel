package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.Bindings;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

public class LightsOnOffHandler implements CommandHandler {


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInSrv()) {
            if (status.isSrvHighBeam()) {
                toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
            } else {
                toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
            }
        }

        if (status.isInMainShip()) {
            toggleLights(Bindings.GameCommand.BINDING_SHIP_LIGHTS_TOGGLE.getGameBinding());
        }
    }

    private void toggleLights(String binding) {
        GameControllerBus.publish(new GameInputEvent(binding, 0));
    }
}
