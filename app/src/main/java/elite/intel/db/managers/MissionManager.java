package elite.intel.db.managers;

import elite.intel.db.dao.MissionDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.util.json.GsonFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MissionManager {
    private static MissionManager instance;

    private MissionManager() {
    }

    public static synchronized MissionManager getInstance() {
        if (instance == null) {
            instance = new MissionManager();
        }
        return instance;
    }

    public void save(MissionDto mission) {
        MissionDao.Mission data = new MissionDao.Mission();
        data.setKey(mission.getMissionId());
        data.setMission(mission.toJson());
        Database.withDao(MissionDao.class, dao -> {
                    dao.upsert(data);
                    return null;
                }
        );
    }

    public MissionDto getMission(long missionId) {
        return Database.withDao(MissionDao.class, dao ->{
            MissionDao.Mission mission = dao.get(missionId);
            if(mission == null) return null;
            return GsonFactory.getGson().fromJson(mission.getMission(), MissionDto.class);
        });
    }

    public Map<Long, MissionDto> getMissions() {
        return Database.withDao(MissionDao.class, dao -> {
            Map<Long, MissionDto> result = new HashMap<>();
            MissionDao.Mission[] missions = dao.listAll();
            for(MissionDao.Mission mission : missions) {
                result.put(mission.getKey(), GsonFactory.getGson().fromJson(mission.getMission(), MissionDto.class));
            }
            return result;
        });
    }

    public Set<String> getTargetFactions() {
        Set<String> result = new HashSet<>();
        Map<Long, MissionDto> missions = getMissions();
        for(MissionDto mission : missions.values()) {
            result.add(mission.getMissionTargetFaction());
        }
        return result;
    }

    public void remove(Long missionId) {
        Database.withDao(MissionDao.class, dao ->{
            dao.delete(missionId);
            return Void.class;
        });
    }
}
