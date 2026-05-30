package elite.intel.ai.hands;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.KeyBindingManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.StringUtls;

import java.util.List;

public class KeyBindCheck {

    private static volatile KeyBindCheck instance;
    private final KeyBindingManager bindingManager = KeyBindingManager.getInstance();

    private KeyBindCheck() {
    }

    public static synchronized KeyBindCheck getInstance() {
        if (instance == null) instance = new KeyBindCheck();
        return instance;
    }

    public void check() {
        BindingsMonitor monitor = BindingsMonitor.getInstance();

        List<String> newMissing = monitor.checkForMissingBindingsAndPersist();
        List<String> newConflicts = monitor.checkForConflictsAndPersist();

        if (!newMissing.isEmpty()) {
            int total = bindingManager.getMissingBindings().size();
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.bindingsMissing", total)
            ));
            newMissing.forEach(m -> EventBusManager.publish(new AppLogEvent("Missing binding: " + m)));
        }

        if (!newConflicts.isEmpty()) {
            int count = newConflicts.size();
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.bindingConflicts", count)
            ));
            newConflicts.forEach(c -> EventBusManager.publish(new AppLogEvent("Binding conflict: " + c)));
        }
    }
}
