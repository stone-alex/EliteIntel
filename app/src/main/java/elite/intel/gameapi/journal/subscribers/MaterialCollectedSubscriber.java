package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.edsm.dto.MaterialsType;
import elite.intel.db.util.Database;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.EDMaterialCaps;
import elite.intel.gameapi.journal.events.MaterialCollectedEvent;

public class MaterialCollectedSubscriber {

    @Subscribe
    public void onMaterialCollected(MaterialCollectedEvent event) {

        String type = determineType(event.getCategory());
        Database.withDao(MaterialsDao.class, dao -> {
            dao.upsert(
                    event.getName(),
                    type,
                    event.getCount(),
                    EDMaterialCaps.getMax(event.getName())
            );
            return null;
        });


        MaterialsDao.Material material = Database.withDao(MaterialsDao.class, dao -> dao.findByExactName(event.getName()));
        EventBusManager.publish(
                new AiVoxResponseEvent(
                        "Collected " + event.getCount() + " units of " + event.getName() + ". Total in storage is " + material.getAmount() + " units."
                )
        );
    }

    private String determineType(String category) {
        if("Raw".equalsIgnoreCase(category)) return MaterialsType.GAME_RAW.getType();
        if("Manufactured".equalsIgnoreCase(category)) return MaterialsType.GAME_MANUFACTURED.getType();
        if("Encoded".equalsIgnoreCase(category)) return MaterialsType.GAME_ENCODED.getType();
        else return MaterialsType.GAME_UNKNOWN.getType();
    }
}
