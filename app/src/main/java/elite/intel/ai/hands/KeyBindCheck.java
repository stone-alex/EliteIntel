package elite.intel.ai.hands;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.KeyBindingManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.AppLogEvent;

import java.util.List;

public class KeyBindCheck {

    private static volatile KeyBindCheck instance;
    private final KeyBindingManager bindingManager = KeyBindingManager.getInstance();

    private KeyBindCheck() {
    }

    public static synchronized KeyBindCheck getInstance() {
        if (instance == null) {
            instance = new KeyBindCheck();
        }

        return instance;
    }

    /*
        Check if there are missing key bindings
    */
    public void check() {
        BindingsMonitor monitor = BindingsMonitor.getInstance();

        List<String> missingBindings = monitor.checkForMissingBindingsAndPersist();
        boolean bindingDiscreppency = missingBindings.isEmpty(); //True if empty

        if (bindingDiscreppency) return;

        StringBuilder missingBindingsMessage = new StringBuilder();
        Integer numberOfBindings = missingBindings.size();
        missingBindingsMessage.append("No Binding found for: ");

        for (String binding : missingBindings) {
            missingBindingsMessage.append(binding);
            missingBindingsMessage.append(", ");
        }

        StringBuilder warning = new StringBuilder();
        String s = numberOfBindings == 1 ? "" : "s";
        warning.append("Commander, there " + (s.equalsIgnoreCase(s) ? "is " : "are") + numberOfBindings + " missing keybinding" + s + ". ");
        warning.append("Until you bind these bindings, i can not fully function in my tasks.");

        EventBusManager.publish(
                new AiVoxResponseEvent(warning.toString())
        );

        EventBusManager.publish(
                new AppLogEvent(missingBindingsMessage.toString())
        );

    }
}