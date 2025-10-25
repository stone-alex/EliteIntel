package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

public class LightsOnOffHandler extends CommandOperator implements CommandHandler {

    public LightsOnOffHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        boolean on = params.get("state").getAsBoolean();

        if (status.isInSrv()) {
            if (on) {
                if (status.isLightsOn()) {
                    return;
                } else {
                    toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                }
            } else {
                if (!status.isLightsOn()) {
                    return;
                } else if (status.isSrvHighBeam()) {
                    toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                } else {
                    toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                    toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                }
            }
        }

        if (status.isInMainShip()) {
            if (on) {
                if (status.isLightsOn()) {
                    return;
                } else {
                    toggleLights(Bindings.GameCommand.BINDING_SHIP_LIGHTS_TOGGLE.getGameBinding());
                }
            } else {
                if (!status.isLightsOn()) {
                    return;
                } else {
                    toggleLights(Bindings.GameCommand.BINDING_SHIP_LIGHTS_TOGGLE.getGameBinding());
                }
            }
        }
    }

    private void toggleLights(String binding) {
        operateKeyboard(binding, 0);
    }
}
