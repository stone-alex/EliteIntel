package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.MaterialManager;
import elite.intel.gameapi.journal.events.EngineerCraftEvent;
import elite.intel.util.StringUtls;

import java.util.List;

public class EngineerCraftEventSubscriber {

    private final MaterialManager materialManager = MaterialManager.getInstance();

    @Subscribe public void onEngineerCraftEvent(EngineerCraftEvent event) {
        List<EngineerCraftEvent.Ingredient> ingredients = event.getIngredients();
        for (EngineerCraftEvent.Ingredient material : ingredients) {
            int count = material.getCount();
            String materialName = material.getNameLocalised() == null ? material.getName() : material.getNameLocalised();
            materialManager.substract(StringUtls.capitalizeWords(materialName), count);
        }
    }
}
