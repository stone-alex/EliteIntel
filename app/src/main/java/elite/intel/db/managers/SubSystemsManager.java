package elite.intel.db.managers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.CommandOperator;
import elite.intel.ai.hands.GameController;
import elite.intel.db.FuzzySearch;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.ShipTargetedEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_CYCLE_NEXT_SUBSYSTEM;

public class SubSystemsManager extends CommandOperator {

    private static volatile SubSystemsManager instance;

    private String target;
    private volatile boolean continueTargeting = true;
    private volatile boolean pause = false;

    private SubSystemsManager(GameController cameController) {
        super(cameController.getMonitor(), cameController.getExecutor());
        EventBusManager.register(this);
    }

    public static SubSystemsManager getInstance(GameController cameController) {
        if (instance == null) {
            synchronized (SubSystemsManager.class) {
                if (instance == null) {
                    instance = new SubSystemsManager(cameController);
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
                    if (cycleCount >= 25) {
                        if (usingFallback) {
                            continueTargeting = false;
                            break;
                        }
                        setTarget("Power Plant");
                        usingFallback = true;
                        cycleCount = 0;
                    }
                    operateKeyboard(BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), 50);
                    pause = true;
                    cycleCount++;
                    SleepNoThrow.sleep(250);
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

        System.out.println("subsystem: " + event.getSubsystemLocalised() + " | target: " + getTarget() + " |");

        if (event.getSubsystemLocalised().equalsIgnoreCase(getTarget())) {
            continueTargeting = false;
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        }
        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        pause = false;
    }
}
