package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.KeyProcessor;
import elite.intel.ai.hands.events.EnterTextEvent;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.hands.events.RawKeyEvent;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.ui.UINavigator;
import elite.intel.util.AudioPlayer;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class RoutePlotter {


    private final UINavigator navigator = new UINavigator();

    public RoutePlotter() {
    }

    public void plotRoute(String destination) {
        navigator.closeOpenPanel();
        if (destination == null || destination.isEmpty()) {
            return;
        }

        String finalDestination = ShipRouteManager.getInstance().getDestination();
        if (finalDestination != null && finalDestination.equalsIgnoreCase(destination)) {
            EventBusManager.publish(new AiVoxResponseEvent("Route already plotted to " + finalDestination + "."));
            return;
        }

        try {
            GameControllerBus.publish(new GameInputEvent(BINDING_GALAXY_MAP.getGameBinding(), 0));
            Thread.sleep(1700);
            GameControllerBus.publish(new GameInputEvent(BINDING_CAM_ZOOM_IN.getGameBinding(), 500));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_LEFT.getGameBinding(), 0));
            Thread.sleep(200);
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_RIGHT.getGameBinding(), 0));
            Thread.sleep(200);
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
            Thread.sleep(200);
            GameControllerBus.publish(new EnterTextEvent(destination));
            Thread.sleep(250);
            GameControllerBus.publish(new RawKeyEvent(KeyProcessor.KEY_DOWNARROW));
            GameControllerBus.publish(new RawKeyEvent(KeyProcessor.KEY_ENTER));
            Thread.sleep(1000);
            GameControllerBus.publish(new RawKeyEvent(KeyProcessor.KEY_ENTER));

            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        } catch (InterruptedException e) {
            //
        }
    }
}
