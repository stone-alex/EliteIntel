package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.custom.CustomCommandOperator;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.LowAltitudeFlightEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.DockedEvent;
import elite.intel.gameapi.journal.events.LiftoffEvent;
import elite.intel.gameapi.journal.events.SupercruiseEntryEvent;
import elite.intel.gameapi.journal.events.TouchdownEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;

import static elite.intel.ai.brain.handlers.CommandHandlerFactory.getInstance;
import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.*;

public class LowAltitudeTrackerSubscriber extends CustomCommandOperator {

    private long lastAnnounceTime = 0;
    private double lastAltitude = -1;
    private boolean powerSetToSystems = false;

    public LowAltitudeTrackerSubscriber() {
        super(getInstance().getGameCommandHandler().getMonitor(), getInstance().getGameCommandHandler().getExecutor());
    }

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {

        boolean isOn = Boolean.valueOf(String.valueOf(PlayerSession.getInstance().get(PlayerSession.LOW_ALTITUDE_FLIGHT)));

        if (isOn && event.getAltitude() > 5 && event.getAltitude() < 2000) {
            // low-altitude flight
            powerToSystems();
            lowAltitudeFlight(event, NavigationUtils.getDirections(0, 0, event));

        } else if (event.getAltitude() > 5 && event.getAltitude() < 200 && NavigationUtils.getDirections(0, 0, event).getSpeed() > 150) {
            long NOW = System.currentTimeMillis();
            if (NOW - lastAnnounceTime > 5_000) {
                EventBusManager.publish(new VoiceProcessEvent("Altitude!. Pull Up!"));
                lastAnnounceTime = NOW;
            }
        }
    }

    private void lowAltitudeFlight(PlayerMovedEvent event, NavigationUtils.Direction navigator) {
        if (lastAltitude < event.getAltitude()) {
            lastAltitude = event.getAltitude();
            return;
        } else {
            lastAltitude = event.getAltitude();
        }

        double speed = navigator.getSpeed();

        if (speed > 150 && event.getAltitude() < 1999 && event.getAltitude() > 1500) {
            String thrustHalf = SET_SPEED75.getGameBinding();
            operateKeyboard(thrustHalf, 1000);
        }

        if (speed > 150 && event.getAltitude() < 1500 && event.getAltitude() > 1000) {
            String thrustHalf = SET_SPEED50.getGameBinding();
            operateKeyboard(thrustHalf, 1000);
        }

        if (speed > 150 && event.getAltitude() < 1000) {
            String thrustQuarter = SET_SPEED25.getGameBinding();
            operateKeyboard(thrustQuarter, 250);
        }


        if (event.getAltitude() < 500 && event.getAltitude() > 300) {
            String upThrust = UP_THRUST_BUTTON.getGameBinding();
            operateKeyboard(upThrust, 250);
        }

        if (event.getAltitude() < 300 && event.getAltitude() > 100) {
            String upThrust = UP_THRUST_BUTTON.getGameBinding();
            operateKeyboard(upThrust, 500);
        }

        if (event.getAltitude() < 100) {
            String upThrust = UP_THRUST_BUTTON.getGameBinding();
            operateKeyboard(upThrust, 1000);
        }
    }

    private static void sleep(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {/**/}
    }

    private void powerToSystems() {
        if (powerSetToSystems) return;

        String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
        String increaseSystemsPower = INCREASE_SYSTEMS_POWER.getGameBinding();
        String increaseEnginesPower = INCREASE_ENGINES_POWER.getGameBinding();

        operateKeyboard(resetPowerDistribution, 0);
        operateKeyboard(increaseSystemsPower, 0);
        operateKeyboard(increaseEnginesPower, 0);
        operateKeyboard(increaseSystemsPower, 0);
        operateKeyboard(increaseEnginesPower, 0);
        operateKeyboard(increaseSystemsPower, 0);

        powerSetToSystems = true;
    }

    private void reset() {
        lastAltitude = -1;
        powerSetToSystems = false;
        lastAnnounceTime = -1;
    }

    @Subscribe
    public void onTouchDownEevent(TouchdownEvent event) {
        reset();
    }

    @Subscribe public void onSuperCruiseEntry(SupercruiseEntryEvent event) {
        reset();
    }

    @Subscribe
    public void onDockedEvent(DockedEvent event) {
        reset();
    }

    @Subscribe
    public void onLiftoffEvent(LiftoffEvent event) {
        reset();
    }

    @Subscribe
    public void onLowAltitude(LowAltitudeFlightEvent event) {
        reset();
    }

}
