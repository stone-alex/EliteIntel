package elite.intel.db.managers;

import elite.intel.db.dao.FsdTargetDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.data.FsdTarget;
import elite.intel.util.json.GsonFactory;

public class FsdTargetManager {
    private static FsdTargetManager instance;

    private FsdTargetManager() {
    }

    public static synchronized FsdTargetManager getInstance() {
        if (instance == null) {
            instance = new FsdTargetManager();
        }
        return instance;
    }

    public void save(FsdTarget fsdTarget) {
        Database.withDao(FsdTargetDao.class, dao -> {
            FsdTargetDao.FsdTarget data = new FsdTargetDao.FsdTarget();
            data.setJson(fsdTarget.toJson());
            dao.save(data);
            return null;
        });

    }

    public FsdTarget get() {
        return Database.withDao(FsdTargetDao.class, dao -> {
            FsdTargetDao.FsdTarget fsdTarget = dao.get();
            if (fsdTarget == null) return null;
            return GsonFactory.getGson().fromJson(fsdTarget.getJson(), FsdTarget.class);
        });
    }
}
