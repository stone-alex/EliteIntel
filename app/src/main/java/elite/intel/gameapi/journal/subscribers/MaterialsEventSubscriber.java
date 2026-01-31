package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.EDMaterialCaps;
import elite.intel.gameapi.journal.events.MaterialsEvent;
import elite.intel.search.edsm.dto.MaterialsType;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.StringUtls;

import java.util.List;

import static elite.intel.search.edsm.dto.MaterialsType.*;

@SuppressWarnings("unused")
public class MaterialsEventSubscriber {

    @Subscribe
    public void onMaterialsEvent(MaterialsEvent event) {
        List<MaterialsEvent.Material> encodedMaterials = event.getEncoded();
        List<MaterialsEvent.Material> manufacturedMaterials = event.getManufactured();
        List<MaterialsEvent.Material> rawMaterials = event.getRaw();

        for (MaterialsEvent.Material material : encodedMaterials) {
            saveMaterial(material, GAME_ENCODED);
        }

        for (MaterialsEvent.Material material : manufacturedMaterials) {
            saveMaterial(material, GAME_MANUFACTURED);
        }

        for (MaterialsEvent.Material material : rawMaterials) {
            saveMaterial(material, GAME_RAW);
        }
    }

    private void saveMaterial(MaterialsEvent.Material material, MaterialsType type) {
        Database.withDao(MaterialsDao.class, dao -> {
            String materialName = StringUtls.capitalizeWords(material.getName());
            dao.upsert(materialName, type.getType(), material.getCount(), EDMaterialCaps.getMax(material.getName()));
            EventBusManager.publish(new AppLogEvent("\tProcessed " + materialName + " " + material.getCount() + " units."));
            return Void.class;
        });
    }
}
