package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MiningAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.ProspectedAsteroidEvent;
import elite.intel.session.PlayerSession;

import java.util.Set;

@SuppressWarnings("unused")
public class ProspectorSubscriber {

    @Subscribe
    public void onProspectedAsteroidEvent(ProspectedAsteroidEvent event) {

        boolean foundTargetMaterial = false;
        StringBuilder sb = new StringBuilder();
        PlayerSession playerSession = PlayerSession.getInstance();

        for (ProspectedAsteroidEvent.Material material : event.getMaterials()) {
            if (material == null) continue;
            if (material.getName() == null || material.getName().isEmpty()) continue;

            String prospectedMaterial = material.getName().toLowerCase();
            Set<String> miningTargets = playerSession.getMiningTargets();

            if (miningTargets.contains(prospectedMaterial)) {
                foundTargetMaterial = true;
                double proportion = material.getProportion();
                String message = "Prospector detected " + String.format("%.2f", proportion) + " percent " + material.getName();
                sb.append(message);
                break;
            }
        }

        if (foundTargetMaterial) {
            EventBusManager.publish(new MiningAnnouncementEvent(sb.toString()));
        }
    }
}
