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
    private boolean continueTargeting = true;
    private boolean pause = false;

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
        setTarget(FuzzySearch.fuzzySubSystemSearch(subsystem, 3));
        continueTargeting = getTarget() != null && !getTarget().isEmpty();

        new Thread(() -> {
            while (continueTargeting) {
                if (!pause) {
                    operateKeyboard(BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), 50);
                    pause = true;
                    SleepNoThrow.sleep(100);
                }
                SleepNoThrow.sleep(10);  // yield + quick flag check
            }
        }, "SubSystemTargeting-Thread").start();
    }

    @Subscribe public void onShipTargetedEvent(ShipTargetedEvent event) {
        if (event == null) {
            return;
        }
        if (event.getSubsystemLocalised() == null) {
            continueTargeting = false;
            return;
        }
        if (event.getSubsystemLocalised().isEmpty()) {
            continueTargeting = false;
            return;
        }

        if (event.getSubsystemLocalised().equalsIgnoreCase(getTarget())) {
            continueTargeting = false;
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        }
        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        pause = false;
    }
}
