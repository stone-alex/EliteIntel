package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.custom.CustomCommandOperator;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;

import static elite.intel.ai.brain.handlers.CommandHandlerFactory.getInstance;
import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.*;

public class LowAltitudeTrackerSubscriber extends CustomCommandOperator {

    private long lastAnnounceTime = 0;

    public LowAltitudeTrackerSubscriber() {
        super(getInstance().getGameCommandHandler().getMonitor(), getInstance().getGameCommandHandler().getExecutor());
    }

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {

        boolean isOn = Boolean.valueOf(String.valueOf(PlayerSession.getInstance().get(PlayerSession.LOW_ALTITUDE_FLIGHT)));

        if (isOn && event.getAltitude() > 5 && event.getAltitude() < 250) {
            // low-altitude flight
            lowAltitudeFlight(event, NavigationUtils.getDirections(0, 0, event));
        } else if(event.getAltitude() > 5 && event.getAltitude() < 200 && NavigationUtils.getDirections(0, 0, event).getSpeed() > 150) {
            long NOW = System.currentTimeMillis();
            if(NOW - lastAnnounceTime > 5_000) {
                EventBusManager.publish(new VoiceProcessEvent("Altitude!. Pull Up!"));
                lastAnnounceTime = NOW;
            }
        }
    }

    private void lowAltitudeFlight(PlayerMovedEvent event, NavigationUtils.Direction navigator) {
        double speed = navigator.getSpeed();
        if (speed > 50) {
            String upThrust = UP_THRUST_BUTTON.getGameBinding();
            operateKeyboard(upThrust, 250);

            if (speed > 150) {
                String thrustZero = SET_SPEED_ZERO.getGameBinding();
                operateKeyboard(thrustZero, 0);
                sleep(750);
                String thrustQuarter = SET_SPEED25.getGameBinding();
                operateKeyboard(thrustQuarter, 0);
            }
        }
    }

    private static void sleep(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {/**/}
    }

}
