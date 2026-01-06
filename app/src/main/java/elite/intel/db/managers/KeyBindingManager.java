package elite.intel.db.managers;

import elite.intel.db.util.Database;
import elite.intel.db.dao.KeyBindingDao;

import java.util.ArrayList;
import java.util.List;

public class KeyBindingManager {
	private static volatile KeyBindingManager instance;

	public static KeyBindingManager getInstance() {
		if (instance == null) {
			instance = new KeyBindingManager();
		}

		return instance;
	}

	// Add a binding
	public void addBinding(String binding) {
		Database.withDao(KeyBindingDao.class, dao -> {
			KeyBindingDao.KeyBinding entity = new KeyBindingDao.KeyBinding();
			entity.setKeyBinding(binding);
			return null;
		});
	}

	// remove a binding
	public void removeBinding(String binding) {
		Database.withDao(KeyBindingDao.KeyBinding.class, dao -> {
			dao.removeBinding(binding);
			return null;
		});
	}

	// clear table data
	public void clear() {
		Database.withDao(KeyBindingDao.KeyBinding.class, dao -> {
			dao.clear();
			return null;
		});
	}

	// Get all bindings
	public List<KeyBindingDao.KeyBinding> getBindings() {
		Database.withDao(KeyBindingDao.KeyBinding.class, dao -> {
			KeyBindingDao.KeyBinding[] bindingsList = dao.getBindings();
			return bindingsList;
		});
	}

}