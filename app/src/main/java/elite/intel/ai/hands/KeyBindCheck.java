package elite.intel.ai.hands;

import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.db.managers.KeyBindingManager;

import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ai.brain.handlers.commands.Bindings;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;

import java.util.List;
import java.util.ArrayList;

public class KeyBindCheck {
	
	private static volatile KeyBindCheck instance; 
	private final KeyBindingManager keyBindingManager = KeyBindingManager.getInstance();
	
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

		boolean bindingDiscreppency = false;
		StringBuilder missingBindings = new StringBuilder();
		missingBindings.append("No Binding found for: [ ");


		for(Bindings.GameCommand command : values) {
			KeyBindingsParser.KeyBinding binding = monitor
			.getBindings()
			.get(command.getGameBinding());

			if(binding == null) {
				missingBindings.append(humanize(command.getGameBinding()));
				missingBindings.append(", ");
				bindingDiscreppency = true;
			}

		}

		if (bindingDiscreppency) {
		
			Integer numberOfBindings = missingBindings.length();

			StringBuilder warning = new StringBuilder();
				warning.append("Commander, there are " + numberOfBindings + " missing keybindings.");
				warning.append("Until you bind these bindings, i can not fully function in my tasks.");

			EventBusManager.publish(
				new AiVoxResponseEvent(warning.toString())
			);

			EventBusManager.publish(
				new AppLogEvent(missingBindings.toString())
			);
		}
	}

	/*
		Temporary class for testing DAO implimentation
	*/
	public void tempCheck() {
		BindingsMonitor monitor = BindingsMonitor.getInstance();
		Bindings.GameCommand[] values = Bindings.GameCommand.values();
		boolean bindingDiscreppency = false;
		
		for(Bindings.GameCommand command : values) {
			KeyBindingsParser.KeyBinding binding = monitor
			.getBindings()
			.get(command.getGameBinding());

			if(binding == null) {
				KeyBindingManager.addBinding(humanize(command.getGameBinding()));
				bindingDiscreppency = true;
			}

		}

		if (bindingDiscreppency) {
		
			ArrayList[] bindings = KeyBindingManager.getBindings();
			Integer numberOfBindings = bindings.length();
			StringBuilder missingBindings = new StringBuilder();
			missingBindings.append("No Binding found for: [ ");

			for (KeyBindingDao.KeyBinding key : bindings) {
				missingBindings.append(key.getKeyBinding());
				missingBindings.append(", ");
			}

			StringBuilder warning = new StringBuilder();
				warning.append("Commander, there are " + numberOfBindings + " missing keybindings.");
				warning.append("Until you bind these bindings, i can not fully function in my tasks.");

			EventBusManager.publish(
				new AiVoxResponseEvent(warning.toString())
			);

			EventBusManager.publish(
				new AppLogEvent(missingBindings.toString())
			);
		}
	}
	/*
		Remove underscores and seperate Camelcase
	*/
	private String humanize(String gameBinding) {
		return gameBinding
			.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
			.replace("HUD", "HUD ")
			.replaceAll("(?<=\\D)(?=\\d)", " ")
			.replaceAll("_", " ");
	}

}