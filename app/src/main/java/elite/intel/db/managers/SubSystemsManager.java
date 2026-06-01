package elite.intel.db.managers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.gameapi.journal.events.ShipTargetedEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.SleepNoThrow;

import java.time.Instant;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_CYCLE_NEXT_SUBSYSTEM;

public class SubSystemsManager {

    private static volatile SubSystemsManager instance;

    private static final int PAUSE_TIMEOUT_MS = 1500;

    private String target;
    private volatile boolean continueTargeting = false;
    private volatile boolean pause = false;
    private volatile Instant lastKeyPressInstant = Instant.EPOCH;

    private SubSystemsManager() {
        EventBusManager.register(this);
    }

    public static SubSystemsManager getInstance() {
        if (instance == null) {
            synchronized (SubSystemsManager.class) {
                if (instance == null) {
                    instance = new SubSystemsManager();
                }
            }
        }
        return instance;
    }

    private String getTarget() {
        return target;
    }

    private void setTarget(String target) {
        this.target = target;
    }

    public void targetSubSystem(String subsystem) {
        pause = false;
        setTarget(FuzzySearch.fuzzySubSystemSearch(subsystem, 2));
        continueTargeting = getTarget() != null && !getTarget().isEmpty();

        new Thread(() -> {
            int cycleCount = 0;
            boolean usingFallback = false;
            while (continueTargeting) {
                if (!pause) {
                    if (!continueTargeting) break;  // re-check after acquiring slot
                    if (cycleCount >= 25) {
                        if (usingFallback) {
                            continueTargeting = false;
                            break;
                        }
                        setTarget("Power Plant");
                        usingFallback = true;
                        cycleCount = 0;
                    }
                    lastKeyPressInstant = Instant.now();
                    GameControllerBus.publish(new GameInputEvent(BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), 50));
                    pause = true;
                    cycleCount++;
                    SleepNoThrow.sleep(250);
                } else if (Instant.now().isAfter(lastKeyPressInstant.plusMillis(PAUSE_TIMEOUT_MS))) {
                    // journal response never arrived; force unpause so cycling can continue
                    pause = false;
                }
                SleepNoThrow.sleep(10);  // yield + quick flag check
            }
        }, "SubSystemTargeting-Thread").start();
    }

    @Subscribe public void onShipTargetedEvent(ShipTargetedEvent event) {
        if (event == null) {
            return;
        }
        if (!event.isTargetLocked()) {
            continueTargeting = false;
            return;
        }
        if (event.getSubsystemLocalised() == null || event.getSubsystemLocalised().isEmpty()) {
            return;
        }
        // only react to events the game wrote after the last key press
        if (Instant.parse(event.getTimestamp()).isBefore(lastKeyPressInstant)) {
            return;
        }

        System.out.println("subsystem: " + event.getSubsystemLocalised() + " | target: " + getTarget() + " |");

        if (event.getSubsystemLocalised().equalsIgnoreCase(getTarget())) {
            continueTargeting = false;
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        } else {
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        }
        pause = false;
    }
}
