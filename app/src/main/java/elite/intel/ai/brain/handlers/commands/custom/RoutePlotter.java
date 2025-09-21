package elite.intel.ai.brain.handlers.commands.custom;

import elite.intel.ai.hands.GameHandler;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.util.AudioPlayer;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.*;

public class RoutePlotter extends CustomCommandOperator {


    public RoutePlotter(GameHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    public void plotRoute(String destination) {
        try {
            String openGalaxyMap = GALAXY_MAP.getGameBinding();
            operateKeyboard(openGalaxyMap, 0);
            Thread.sleep(200);
            Thread.sleep(1500);
            String uiLeft = UI_LEFT.getGameBinding();
            operateKeyboard(uiLeft, 0);
            Thread.sleep(200);
            String uiRight = UI_RIGHT.getGameBinding();
            operateKeyboard(uiRight, 0);
            Thread.sleep(200);
            String activate = UI_ACTIVATE.getGameBinding();
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
            operateKeyboard(CAM_ZOOM_OUT.getGameBinding(), 120);
            Thread.sleep(200);
            operateKeyboard(CAM_ZOOM_IN.getGameBinding(), 120);

            Thread.sleep(200);
            keyProcessor.pressAndHoldKey(KeyProcessor.KEY_ENTER, 3000);

            operateKeyboard(openGalaxyMap, 0);
        } catch (InterruptedException e) {
            //
        }
    }

}
