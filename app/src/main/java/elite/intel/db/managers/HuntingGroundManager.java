package elite.intel.db.managers;

import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.dao.PirateHuntingGroundsDao;
import elite.intel.db.dao.PirateHuntingGroundsDao.HuntingGround;
import elite.intel.db.dao.PirateMissionProviderDao;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.starsystems.StarSystemResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HuntingGroundManager {

    private static volatile HuntingGroundManager instance;

    private HuntingGroundManager() {
        // Private constructor to prevent instantiation
    }

    public static HuntingGroundManager getInstance() {
        if (instance == null) {
            synchronized (HuntingGroundManager.class) {
                if (instance == null) {
                    instance = new HuntingGroundManager();
                }
            }
        }
        return instance;
    }


    /// Saves partial we do not have faction names from INTRA data.
    public PirateMissionTuple<HuntingGround, List<MissionProvider>>
    savePartialPair(StarSystemResult.SystemRecord provider, StarSystemResult.SystemRecord target) {

        HuntingGround pirateFaction = Database.withDao(PirateHuntingGroundsDao.class, dao -> {
            HuntingGround targetEntity = new HuntingGround();
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

    public List<PirateMissionTuple<HuntingGround, List<MissionProvider>>> findInProviderForTargetStarSystem(String targetStarSystem) {
        HuntingGround targetEntity = Database.withDao(
                PirateHuntingGroundsDao.class, dao -> dao.findByStarSystem(targetStarSystem)
        );

        // nothing found, return empty list, and fall back to net search
        if (targetEntity == null) return new ArrayList<>();

        List<MissionProvider> providers = getMissionProviders(targetEntity);
        return Collections.singletonList(new PirateMissionTuple<>(targetEntity, providers));
    }


    public List<PirateMissionTuple<HuntingGround, List<MissionProvider>>> findTargetSystemInRangeForRecon(LocationDao.Coordinates coordinates) {
        HuntingGround targetEntity = Database.withDao(
                PirateHuntingGroundsDao.class, dao -> dao.findNearestRecon(coordinates.x(), coordinates.y(), coordinates.z())
        );

        // nothing found, return empty list, and fall back to net search
        if (targetEntity == null) return new ArrayList<>();

        List<MissionProvider> providers = getMissionProviders(targetEntity);
        return Collections.singletonList(new PirateMissionTuple<>(targetEntity, providers));
    }



    private List<MissionProvider> getMissionProviders(HuntingGround targetEntity) {
        if (targetEntity == null) return new ArrayList<>();
        List<MissionProvider> providers = Database.withDao(
                PirateMissionProviderDao.class, dao -> dao.findMissionProviderForTargetFaction(targetEntity.getId())
        );
        return providers;
    }

    public int updateTargetFaction(String destinationSystem, String targetFaction) {
        if (targetFaction == null || destinationSystem == null) return 0;

        Database.withDao(PirateHuntingGroundsDao.class, dao -> {
            dao.updateTargetFaction(destinationSystem, targetFaction);
            return Void.class;
        });

        return Database.withDao(PirateHuntingGroundsDao.class, dao -> dao.findByFactionName(targetFaction, destinationSystem).getId());
    }

    public void updateProviderFaction(String providerStarSystem, int targetFactionId, String providerFaction) {
        if (providerStarSystem == null || targetFactionId == 0 || providerFaction == null) return;

        Database.withDao(PirateMissionProviderDao.class, dao -> {
            MissionProvider provider = dao.findNullForTarget(providerStarSystem, targetFactionId);
            if (provider == null) return Void.class;
            dao.updateFaction(provider.getId(), providerFaction);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Mission provider faction updated. " + providerFaction));
            return Void.class;
        });
    }

    public String findStarSystemForFactionName(String targetFaction) {
        return Database.withDao(PirateHuntingGroundsDao.class, dao -> dao.findByFactionName(targetFaction));
    }

    public void ignoreHuntingGround(String starSystemName) {
        Database.withDao(PirateHuntingGroundsDao.class, dao -> {
            dao.ignoreHuntingGround(starSystemName);
            return Void.TYPE;
        });
    }

    public void confirmTargetReconResourceSite(String primaryStarName) {
        Database.withDao(PirateHuntingGroundsDao.class, dao -> {
            HuntingGround system = dao.findByStarSystem(primaryStarName);
            if (system != null && !system.isHasResSite()) {
                dao.confirm(primaryStarName);
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Recon confirmed for " + primaryStarName + ". Resource Sites Found"));
            }
            return Void.class;
        });
    }

    public void clear() {
        Database.withDao(PirateHuntingGroundsDao.class, dao -> {
            dao.clear();
            return Void.TYPE;
        });
    }


    public static class PirateMissionTuple<K, V> {
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
