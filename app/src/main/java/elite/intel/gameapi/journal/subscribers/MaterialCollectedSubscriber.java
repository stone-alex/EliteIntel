package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.edsm.dto.MaterialsDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.EDMaterialCaps;
import elite.intel.gameapi.journal.events.MaterialCollectedEvent;
import elite.intel.session.MaterialsData;
import elite.intel.util.StringUtls;

import java.util.ArrayList;
import java.util.List;

public class MaterialCollectedSubscriber {

    @Subscribe
    public void onMaterialCollected(MaterialCollectedEvent event) {


        boolean materialExists = false;
        MaterialsData mats = MaterialsData.getInstance();
        MaterialsDto data = mats.getMaterials();
        List<MaterialsDto.MaterialEntry> existingEntries = data.getMaterials();
        List<MaterialsDto.MaterialEntry> updatedEntries = new ArrayList<>();

        for (MaterialsDto.MaterialEntry entry : existingEntries) {
            if (entry.getMaterialName().equalsIgnoreCase(event.getName())) {
                materialExists = true;
                MaterialsDto.MaterialEntry materialEntry = new MaterialsDto.MaterialEntry();
                materialEntry.setMaterialName(StringUtls.capitalizeWords(event.getName()));
                int total = event.getCount() + materialEntry.getQuantity();
                materialEntry.setQuantity(total);
                updatedEntries.add(materialEntry);
            } else {
                updatedEntries.add(entry);
            }
        }

        if(!materialExists) {
            MaterialsDto.MaterialEntry materialEntry = new MaterialsDto.MaterialEntry();
            materialEntry.setMaterialName(StringUtls.capitalizeWords(event.getName()));
            materialEntry.setQuantity(event.getCount());
            updatedEntries.add(materialEntry);
        }

        data.setMaterials(updatedEntries);
        mats.setMaterialsDto(data);
        EventBusManager.publish(new AiVoxResponseEvent("Collected " + event.getCount()+" units of "+event.getName()+"."));
    }
}
