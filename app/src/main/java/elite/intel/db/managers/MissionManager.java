package elite.intel.db.managers;

import elite.intel.db.dao.MissionDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.MissionType;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.util.json.GsonFactory;

import java.util.*;

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
        data.setMissionType(mission.getMissionType());
        Database.withDao(MissionDao.class, dao -> {
                    dao.upsert(data, data.getMissionType().name());
                    return null;
                }
        );
    }

    public MissionDto getMission(long missionId) {
        return Database.withDao(MissionDao.class, dao -> {
            MissionDao.Mission mission = dao.get(missionId);
            if (mission == null) return null;
            return GsonFactory.getGson().fromJson(mission.getMission(), MissionDto.class);
        });
    }

    public Map<Long, MissionDto> getMissions(List<String> missionTypes) {
        return Database.withDao(MissionDao.class, dao -> {
            Map<Long, MissionDto> result = new HashMap<>();
            List<MissionDao.Mission> missions = dao.listAll(missionTypes);
            missions.sort(Comparator.comparing(MissionDao.Mission::getKey));
            for (MissionDao.Mission mission : missions) {
                result.put(mission.getKey(), GsonFactory.getGson().fromJson(mission.getMission(), MissionDto.class));
            }
            return result;
        });
    }

    public Set<String> getTargetFactions(List<String> missionTypes) {
        Set<String> result = new HashSet<>();
        Map<Long, MissionDto> missions = getMissions(missionTypes);
        for (MissionDto mission : missions.values()) {
            result.add(mission.getMissionTargetFaction());
        }
        return result;
    }

    public void remove(Long missionId) {
        Database.withDao(MissionDao.class, dao -> {
            dao.delete(missionId);
            return Void.class;
        });
    }

    public List<String> getPirateMissionTypes() {
        return new ArrayList<>(
                Arrays.asList(
                        MissionType.MISSION_PIRATE_MASSACRE.name(),
                        MissionType.MISSION_PIRATE_MASSACRE_WING.name(),
                        MissionType.MISSION_PIRATE_MASSACRE_WING_NAME.name()
                )
        );
    }
}
