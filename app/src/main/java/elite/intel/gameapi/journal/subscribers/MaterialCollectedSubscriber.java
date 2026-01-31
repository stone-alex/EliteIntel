package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.db.managers.MaterialManager;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.MaterialCollectedEvent;
import elite.intel.search.edsm.dto.MaterialsType;
import elite.intel.util.StringUtls;

public class MaterialCollectedSubscriber {

    private final MaterialManager materialManager = MaterialManager.getInstance();

    @Subscribe
    public void onMaterialCollected(MaterialCollectedEvent event) {
        materialManager.save(event.getName(), determineType(event.getCategory()), event.getCount());
        MaterialsDao.Material material = Database.withDao(MaterialsDao.class, dao -> dao.findByExactName(StringUtls.capitalizeWords(event.getName())));
        EventBusManager.publish(
                new AiVoxResponseEvent(
                        "Collected " + event.getCount() + " units of " + event.getName() + (material == null ? "." : ". Total in storage is " + material.getAmount() + " units.")
                )
        );
    }

    private MaterialsType determineType(String category) {
        if ("Raw".equalsIgnoreCase(category)) return MaterialsType.GAME_RAW;
        if ("Manufactured".equalsIgnoreCase(category)) return MaterialsType.GAME_MANUFACTURED;
        if ("Encoded".equalsIgnoreCase(category)) return MaterialsType.GAME_ENCODED;
        else return MaterialsType.GAME_UNKNOWN;
    }
}
