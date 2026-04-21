package elite.intel.db.managers;

import elite.intel.db.dao.BindingConflictDao;
import elite.intel.db.util.Database;

import java.util.List;

public class BindingConflictManager {

    private static volatile BindingConflictManager instance;

    private BindingConflictManager() {
    }

    public static BindingConflictManager getInstance() {
        if (instance == null) {
            synchronized (BindingConflictManager.class) {
                if (instance == null) instance = new BindingConflictManager();
            }
        }
        return instance;
    }

    public void save(String conflictKey, String description) {
        Database.withDao(BindingConflictDao.class, dao -> {
            BindingConflictDao.ConflictRecord r = new BindingConflictDao.ConflictRecord();
            r.setConflictKey(conflictKey);
            r.setDescription(description);
            dao.save(r);
            return Void.TYPE;
        });
    }

    public void remove(String conflictKey) {
        Database.withDao(BindingConflictDao.class, dao -> {
            dao.remove(conflictKey);
            return Void.TYPE;
        });
    }

    public List<BindingConflictDao.ConflictRecord> getConflicts() {
        return Database.withDao(BindingConflictDao.class, BindingConflictDao::listAll);
    }
}
