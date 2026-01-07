package elite.intel.db.managers;

import elite.intel.db.dao.KeyBindingDao;
import elite.intel.db.dao.KeyBindingDao.KeyBinding;
import elite.intel.db.util.Database;

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
            KeyBinding entity = new KeyBinding();
            entity.setKeyBinding(binding);
            dao.save(entity);
            return Void.class;
        });
    }

    // remove a binding
    public void removeBinding(String binding) {
        Database.withDao(KeyBindingDao.class, dao -> {
            dao.removeBinding(binding);
            return Void.class;
        });
    }

    // clear table data
    public void clear() {
        Database.withDao(KeyBindingDao.class, dao -> {
            dao.clear();
            return Void.class;
        });
    }

    // Get all bindings
    public List<KeyBinding> getBindings() {
        return Database.withDao(KeyBindingDao.class, dao -> dao.listAll());
    }

}