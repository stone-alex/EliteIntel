package elite.intel.db.managers;

import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.dao.PirateFactionDao;
import elite.intel.db.dao.PirateFactionDao.PirateFaction;
import elite.intel.db.dao.PirateMissionProviderDao;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.starsystems.StarSystemResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PirateMissionDataManager {

    private static volatile PirateMissionDataManager instance;

    private PirateMissionDataManager() {
        // Private constructor to prevent instantiation
    }

    public static PirateMissionDataManager getInstance() {
        if (instance == null) {
            synchronized (PirateMissionDataManager.class) {
                if (instance == null) {
                    instance = new PirateMissionDataManager();
                }
            }
        }
        return instance;
    }


    /// Saves partial we do not have faction names from INTRA data.
    public PirateMissionTuple<PirateFaction, List<MissionProvider>>
    savePartialPair(StarSystemResult.SystemRecord provider, StarSystemResult.SystemRecord target) {

        PirateFaction pirateFaction = Database.withDao(PirateFactionDao.class, dao -> {
            PirateFaction targetEntity = new PirateFaction();
            targetEntity.setStarSystem(target.getName());
            targetEntity.setX(target.getX());
            targetEntity.setY(target.getY());
            targetEntity.setZ(target.getZ());
            dao.save(targetEntity);
            return dao.findByStarSystem(target.getName());
        });

        List<MissionProvider> providers = Database.withDao(PirateMissionProviderDao.class, dao -> {
            MissionProvider entity = new MissionProvider();
            entity.setTargetFactionID(pirateFaction.getId());
            entity.setStarSystem(provider.getName());
            entity.setX(provider.getX());
            entity.setY(provider.getY());
            entity.setZ(provider.getZ());
            dao.upsert(entity);
            return dao.findMissionProviderForTargetFaction(pirateFaction.getId());
        });

        return new PirateMissionTuple<>(pirateFaction, providers);
    }

    public List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> findInRange(LocationDao.Coordinates coordinates, int range) {
        PirateFaction targetEntity = Database.withDao(
                PirateFactionDao.class, dao -> dao.findNearest(coordinates.x(), coordinates.y(), coordinates.z(), range)
        );

        // nothing found, return empty list, and fall back to net search
        if (targetEntity == null) return new ArrayList<>();

        List<MissionProvider> providers = getMissionProviders(targetEntity);
        return Collections.singletonList(new PirateMissionTuple<>(targetEntity, providers));
    }


    public List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> findInRangeForRecon(LocationDao.Coordinates coordinates, int range) {
        PirateFaction targetEntity = Database.withDao(
                PirateFactionDao.class, dao -> dao.findNearestRecon(coordinates.x(), coordinates.y(), coordinates.z(), range)
        );

        // nothing found, return empty list, and fall back to net search
        if (targetEntity == null) return new ArrayList<>();

        List<MissionProvider> providers = getMissionProviders(targetEntity);
        return Collections.singletonList(new PirateMissionTuple<>(targetEntity, providers));
    }


    private List<MissionProvider> getMissionProviders(PirateFaction targetEntity) {
        if(targetEntity == null) return new ArrayList<>();
        List<MissionProvider> providers = Database.withDao(
                PirateMissionProviderDao.class, dao -> dao.findMissionProviderForTargetFaction(targetEntity.getId())
        );
        return providers;
    }

    public int updateTargetFaction(String destinationSystem, String targetFaction) {
        if(targetFaction == null || destinationSystem == null) return 0;

        Database.withDao(PirateFactionDao.class, dao -> {
            dao.updateTargetFaction(destinationSystem, targetFaction);
            return Void.class;
        });

        return Database.withDao(PirateFactionDao.class, dao -> dao.findByFactionName(targetFaction, destinationSystem).getId());
    }

    public void updateProviderFaction(String providerStarSystem, int targetFactionId, String providerFaction) {
        if(providerStarSystem == null || targetFactionId == 0 || providerFaction == null) return;

        Database.withDao(PirateMissionProviderDao.class, dao -> {
            MissionProvider provider = dao.findNullForTarget(providerStarSystem, targetFactionId);
            if(provider == null) return Void.class;
            dao.updateFaction(provider.getId(), providerFaction);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Mission provider faction updated. " + providerFaction));
            return Void.class;
        });
    }

    public PirateFaction findByFactionName(String targetFaction, String targetSystem) {
        return Database.withDao(PirateFactionDao.class, dao -> dao.findByFactionName(targetFaction, targetSystem));
    }

    public void confirmTargetReconResourceSite(String primaryStarName) {
        Database.withDao(PirateFactionDao.class, dao -> {
            PirateFaction system = dao.findByStarSystem(primaryStarName);
            if (system != null &&  !system.isHasResSite()) {
                dao.confirm(primaryStarName);
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Recon confirmed for " + primaryStarName + ". Resource Sites Found"));
            }
            return Void.class;
        });
    }


    public class PirateMissionTuple<K, V> {
        K target;
        V missionProvider;

        PirateMissionTuple(K key, V value) {
            this.target = key;
            this.missionProvider = value;
        }

        public K getTarget() {
            return target;
        }

        public V getMissionProvider() {
            return missionProvider;
        }
    }
}
