package elite.intel.db.managers;

import elite.intel.db.dao.ReputationDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.ReputationEvent;
import elite.intel.util.json.GsonFactory;

public class ReputationManager {
    private static ReputationManager instance;

    private ReputationManager() {
    }

    public static synchronized ReputationManager getInstance() {
        if (instance == null) {
            instance = new ReputationManager();
        }
        return instance;
    }

    public void save(ReputationEvent event) {
        Database.withDao(ReputationDao.class, dao -> {
            ReputationDao.Reputation data = new ReputationDao.Reputation();
            data.setJson(event.toJson());
            dao.save(data);
            return null;
        });
    }

    public ReputationEvent get() {
        return Database.withDao(ReputationDao.class, dao ->{
            ReputationDao.Reputation reputation = dao.get();
            if(reputation == null) return null;
            return GsonFactory.getGson().fromJson(reputation.getJson(), ReputationEvent.class);
        });
    }
}
