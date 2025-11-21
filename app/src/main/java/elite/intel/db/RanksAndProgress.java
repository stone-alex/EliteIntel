package elite.intel.db;

import elite.intel.db.dao.RankAndProgressDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.util.json.GsonFactory;

public final class RanksAndProgress {
    private static RanksAndProgress instance;

    private RanksAndProgress() {
    }

    public static synchronized RanksAndProgress getInstance() {
        if (instance == null) {
            instance = new RanksAndProgress();
        }
        return instance;
    }

    public RankAndProgressDto get() {
        return Database.withDao(
                RankAndProgressDao.class,
                dao -> {
                    RankAndProgressDao.RankAndProgress progress = dao.get();
                    if (progress == null) return new RankAndProgressDto();
                    return GsonFactory.getGson().fromJson(progress.getJson(),
                            RankAndProgressDto.class
                    );
                }
        );
    }

    public void save(RankAndProgressDto data) {
        Database.withDao(RankAndProgressDao.class, dao -> {
            RankAndProgressDao.RankAndProgress rankAndProgress = new RankAndProgressDao.RankAndProgress();
            rankAndProgress.setJson(data.toJson());
            dao.save(rankAndProgress);
            return null;
        });
    }
}
