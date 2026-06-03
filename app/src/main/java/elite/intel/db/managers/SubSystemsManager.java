package elite.intel.db.managers;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.FuzzySearch;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.gameapi.journal.events.ShipTargetedEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.SleepNoThrow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_CYCLE_NEXT_SUBSYSTEM;

public class SubSystemsManager {

    private static final Logger log = LogManager.getLogger(SubSystemsManager.class);
    private static volatile SubSystemsManager instance;

    private static final int PAUSE_TIMEOUT_MS = 1500;

    private String target;
    private volatile boolean continueTargeting = false;
    private volatile boolean pause = false;
    private volatile int consecutiveTimeouts = 0;
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
        log.debug("[1] targetSubSystem raw input: [{}]", subsystem);
        pause = false;
        String resolved = FuzzySearch.fuzzySubSystemSearch(subsystem, 4);
        log.debug("[2] fuzzy resolved: [{}]", resolved);
        setTarget(resolved);
        continueTargeting = getTarget() != null && !getTarget().isEmpty();

        if (!continueTargeting) {
            log.debug("[3] no fuzzy match - cycling will NOT start");
            return;
        }
        log.debug("[3] target set to [{}] - cycling starting", getTarget());

        consecutiveTimeouts = 0;
        new Thread(() -> {
            int cycleCount = 0;
            boolean usingFallback = false;
            while (continueTargeting) {
                if (!pause) {
                    if (!continueTargeting) break;
                    if (cycleCount >= 25) {
                        if (usingFallback) {
                            log.debug("[cycle] exhausted fallback - giving up");
                            continueTargeting = false;
                            break;
                        }
                        log.debug("[cycle] limit reached - falling back to Power Plant");
                        setTarget("Power Plant");
                        usingFallback = true;
                        cycleCount = 0;
                    }
                    lastKeyPressInstant = Instant.now();
                    log.debug("[cycle] count={} pressing key, target=[{}]", cycleCount, getTarget());
                    GameControllerBus.publish(GameInputSequenceEvent.of(
                            GameInputStep.bindingHold(BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), 50),
                            GameInputStep.delay(250)
                    ));
                    pause = true;
                    cycleCount++;
                } else if (Instant.now().isAfter(lastKeyPressInstant.plusMillis(PAUSE_TIMEOUT_MS))) {
                    consecutiveTimeouts++;
                    log.debug("[cycle] journal timeout #{} after {}ms - no ShipTargeted event received", consecutiveTimeouts, PAUSE_TIMEOUT_MS);
                    if (consecutiveTimeouts >= 3) {
                        log.debug("[cycle] 3 consecutive timeouts - assuming target lost, stopping");
                        continueTargeting = false;
                        break;
                    }
                    pause = false;
                }
                SleepNoThrow.sleep(10);
            }
        }, "SubSystemTargeting-Thread").start();
    }

    @Subscribe public void onShipTargetedEvent(ShipTargetedEvent event) {
        if (event == null) {
            return;
        }
        if (!event.isTargetLocked()) {
            log.debug("[journal] target lock lost - stopping");
            continueTargeting = false;
            return;
        }
        if (event.getSubsystemLocalised() == null || event.getSubsystemLocalised().isEmpty()) {
            log.debug("[journal] ShipTargeted has no subsystem (scanStage={})", event.getScanStage());
            return;
        }

        Instant eventInstant = Instant.parse(event.getTimestamp());
        Instant keyPressFloor = lastKeyPressInstant.truncatedTo(ChronoUnit.SECONDS);
        if (eventInstant.isBefore(keyPressFloor)) {
            log.debug("[journal] stale event filtered: eventTime=[{}] keyPressFloor=[{}]", eventInstant, keyPressFloor);
            return;
        }

        String raw = event.getSubsystemLocalised();
        String trimmed = raw.trim();
        log.debug("[journal] game=[{}] (len={}) trimmed=[{}] (len={}) target=[{}] (len={})",
                raw, raw.length(), trimmed, trimmed.length(), getTarget(), getTarget() == null ? -1 : getTarget().length());

        if (trimmed.equalsIgnoreCase(getTarget())) {
            log.debug("[journal] MATCH - stopping");
            continueTargeting = false;
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        } else {
            log.debug("[journal] no match - continuing");
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        }
        consecutiveTimeouts = 0;
        pause = false;
    }
}
