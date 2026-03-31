package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.GameController;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.ai.hands.events.EnterTextEvent;
import elite.intel.ai.hands.events.RawKeyEvent;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.ui.UINavigator;
import elite.intel.util.AudioPlayer;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class RoutePlotter extends CommandOperator {


    private final UINavigator navigator = new UINavigator(this);
    public RoutePlotter(GameController cameController) {
        super(cameController.getMonitor(), cameController.getExecutor());
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
            String openGalaxyMap = BINDING_GALAXY_MAP.getGameBinding();
            operateKeyboard(openGalaxyMap, 0);
            Thread.sleep(200);
            Thread.sleep(1500);
            operateKeyboard(BINDING_CAM_ZOOM_IN.getGameBinding(), 500);
            String uiLeft = BINDING_UI_LEFT.getGameBinding();
            operateKeyboard(uiLeft, 0);
            Thread.sleep(200);
            String uiRight = BINDING_UI_RIGHT.getGameBinding();
            operateKeyboard(uiRight, 0);
            Thread.sleep(200);
            String activate = BINDING_ACTIVATE.getGameBinding();
            operateKeyboard(activate, 0);
            Thread.sleep(200);
            GameControllerBus.publish(new EnterTextEvent(destination));
            Thread.sleep(250);
            GameControllerBus.publish(new RawKeyEvent(KeyProcessor.KEY_DOWNARROW));
            GameControllerBus.publish(new RawKeyEvent(KeyProcessor.KEY_ENTER));
            Thread.sleep(1000);
            GameControllerBus.publish(new RawKeyEvent(KeyProcessor.KEY_ENTER));

//            //glide time
//            operateKeyboard(BINDING_CAM_ZOOM_OUT.getGameBinding(), 60);
//            Thread.sleep(4500);
//            //Game bug work around
//            operateKeyboard(BINDING_CAM_ZOOM_OUT.getGameBinding(), 120);
//            Thread.sleep(200);
//            operateKeyboard(BINDING_CAM_ZOOM_IN.getGameBinding(), 120);
//
//
//            Thread.sleep(300);
//            keyProcessor.pressAndHoldKey(KeyProcessor.KEY_ENTER, 2500);
//            Thread.sleep(500);
//
//            //operateKeyboard(openGalaxyMap, 0);
//            operateKeyboard(BINDING_CAM_ZOOM_OUT.getGameBinding(), 1000);
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        } catch (InterruptedException e) {
            //
        }
    }
}
