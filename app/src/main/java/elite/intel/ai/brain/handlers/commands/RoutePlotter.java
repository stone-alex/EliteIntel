package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.GameController;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.util.AudioPlayer;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class RoutePlotter extends CommandOperator {


    public RoutePlotter(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    public void plotRoute(String destination) {
        try {
            String openGalaxyMap = BINDING_GALAXY_MAP.getGameBinding();
            operateKeyboard(openGalaxyMap, 0);
            Thread.sleep(200);
            Thread.sleep(1500);
            String uiLeft = BINDING_UI_LEFT.getGameBinding();
            operateKeyboard(uiLeft, 0);
            Thread.sleep(200);
            String uiRight = BINDING_UI_RIGHT.getGameBinding();
            operateKeyboard(uiRight, 0);
            Thread.sleep(200);
            String activate = BINDING_ACTIVATE.getGameBinding();
            operateKeyboard(activate, 0);
            Thread.sleep(200);
            KeyProcessor keyProcessor = KeyProcessor.getInstance();
            keyProcessor.enterText(destination);
            Thread.sleep(200);

            keyProcessor.pressKey(KeyProcessor.KEY_ENTER);
            keyProcessor.pressKey(KeyProcessor.KEY_ENTER);
            AudioPlayer.getInstance().playBeep();

            Thread.sleep(200);
            keyProcessor.pressAndHoldKey(KeyProcessor.KEY_ENTER, 2500);
            Thread.sleep(500);

            AudioPlayer.getInstance().playBeep();

            //Game bug work around
            operateKeyboard(BINDING_CAM_ZOOM_OUT.getGameBinding(), 120);
            Thread.sleep(200);
            operateKeyboard(BINDING_CAM_ZOOM_IN.getGameBinding(), 120);

            Thread.sleep(200);
            keyProcessor.pressAndHoldKey(KeyProcessor.KEY_ENTER, 3000);

            operateKeyboard(openGalaxyMap, 0);
        } catch (InterruptedException e) {
            //
        }
    }

}
