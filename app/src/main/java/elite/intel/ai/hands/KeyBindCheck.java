package elite.intel.ai.hands;

/*
Feature/Improvement:
Check key bindings, and warn user via UI terminal about missing key binds.

    - Implement KeybindCheck class as true singleton running in it’s own thread.
    - Class location is elite.intel.ai.hands package
    - Wireing start to the service start method in AppController::startStopServices
    - Check for bindings. Use something like this
    See KeyBindingExecutor lines 34 through 46 on how to construct the keybinding map

    Publish list to the console:
	EventBusManager.publish(new AppLogEvent( 
		formatted and human readable string containing your list of missing key bindings. 
		debug console supports basic HTML or use Ascii art. ));
*/
/*
	Integer mainKeyCode = ELITE_TO_KEYPROCESSOR_MAP.get(binding.key.toUpperCase());
            if (mainKeyCode == null) {
                     //add to list
            }
*/

import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.brain.handlers.commands.Bindings;
import elite.intel.ui.event.AppLogEvent;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager; 

import java.util.List;
import java.util.ArrayList;

public class KeyBindCheck {
	private static KeyBindCheck instance;

	private KeyBindCheck () {}

	public static synchronized KeyBindCheck getInstance() {
		if(instance == null) {
			instance = new KeyBindCheck();
		}

		return instance;
	}

	/*
		Check if there are missing key bindings
	*/
	public void check() {
		BindingsMonitor monitor = BindingsMonitor.getInstance();
		Bindings.GameCommand[] values = Bindings.GameCommand.values();
		ArrayList<String> missingBindings = new ArrayList();

		for(Bindings.GameCommand command : values) {
			KeyBindingsParser.KeyBinding binding = monitor
			.getBindings()
			.get(command.getGameBinding());

			if(binding == null) {
				missingBindings.add(command.getGameBinding());
			}

		}

		if (missingBindings.size() > 0) {
			StringBuilder warning = new StringBuilder();
				warning.append("Commander, there are " + missingBindings.size() + " missing keybindings.");
				warning.append("Until you bind these bindings, i can not fully function in my tasks.");
			EventBusManager.publish(
				new AiVoxResponseEvent(warning.toString())
			);
			EventBusManager.publish(
				new AppLogEvent(
					"No Binding found for: [ " + 
					String.join(", ", missingBindings) + " ]")
			);
		}
	}

	/*
		TOOD: Register misisng keys to databsae
	*/
	private void saveBinding() {}

	/*
		TODO: Remove a binding from the database when bound ands/or wipe the db when app launches
	*/
	private void clearBindingFromDb() {}

	/*
		TODO: Respond to user prompt for next missing keybind
	*/
	public void getNextBinding() {}

}