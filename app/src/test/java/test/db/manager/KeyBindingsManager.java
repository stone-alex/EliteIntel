package test.db.managers;

import java.util.ArrayList;
import java.util.List;

import elite.intel.db.util.Database;
import elite.intel.ai.mouth.subscribers.events.*;

import test.db.dao.BindingsDao;

public class KeyBindingsManager {
	private static KeyBindingsManager instance;
	private KeyBindingsManager(){}

	public static synchronized KeyBindingsManager getInstance(){
		if (instance == null) {
			instance = new KeyBindingsManager();
		}
		return instance;
	}

	public void addMisingBinding(KeyBinding command) {

	}

	public KeyBinding getNextBinding(Integer id) {
		
	}

	public KeyBinding getBinding(KeyBinding command) {}

	public void deleteBinding(KeyBinding command) {}
}