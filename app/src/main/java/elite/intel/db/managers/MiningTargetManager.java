package elite.intel.db.managers;

import elite.intel.db.dao.MiningTargetDao;
import elite.intel.db.util.Database;

import java.util.Set;

public class MiningTargetManager {
    private static final MiningTargetManager INSTANCE = new MiningTargetManager();

    private MiningTargetManager() {
    }

    public static MiningTargetManager getInstance() {
        return INSTANCE;
    }


    public Set<String> getAll() {
        return Database.withDao(MiningTargetDao.class, dao -> {
            Set<String> result = new java.util.HashSet<>();
            MiningTargetDao.MiningTarget[] miningTargets = dao.listAll();
            for (MiningTargetDao.MiningTarget miningTarget : miningTargets) {
                result.add(miningTarget.getTarget());
            }
            return result;
        });
    }


    public void add(String miningTarget) {
        if (miningTarget == null || miningTarget.isEmpty()) {
            return;
        }

        Database.withDao(MiningTargetDao.class, dao -> {
            MiningTargetDao.MiningTarget target = new MiningTargetDao.MiningTarget();
            target.setTarget(miningTarget);
            dao.add(target);
            return null;
        });
    }

    public void clear() {
        Database.withDao(MiningTargetDao.class, dao ->{
            dao.clear();
            return null;
        });
    }
}
