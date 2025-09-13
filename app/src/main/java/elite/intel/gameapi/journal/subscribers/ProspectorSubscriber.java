package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.custom.CustomCommands;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ProspectedAsteroidEvent;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class ProspectorSubscriber {

    @Subscribe
    public void onProspectedAsteroidEvent(ProspectedAsteroidEvent event) {

        boolean foundTargetMaterial = false;
        StringBuilder anouncement = new StringBuilder();

        for (ProspectedAsteroidEvent.Material material : event.getMaterials()) {
            String prospectedMaterial = material == null ? "" : material.getName();
            String targetMaterial = String.valueOf(PlayerSession.getInstance().get(CustomCommands.SET_MINING_TARGET.getParamKey())).replaceAll("\"", "");
            if (prospectedMaterial != null && !prospectedMaterial.isEmpty() && prospectedMaterial.toLowerCase().equals(targetMaterial.toLowerCase())) {
                foundTargetMaterial = true;

                double proportion = material.getProportion();
                String message = "Prospector detected " + String.format("%.2f", proportion) + " percent " + material.getName();
                anouncement.append(message);

                break;
            }
        }

        if (foundTargetMaterial) {
            EventBusManager.publish(new VoiceProcessEvent(anouncement.toString()));
        }

    }

}
