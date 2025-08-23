package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import elite.companion.EventBusManager;
import elite.companion.comms.GrokInteractionHandler;
import elite.companion.comms.VoiceNotifier;
import elite.companion.events.ProspectedAsteroidEvent;
import elite.companion.session.SessionTracker;

import java.util.Arrays;

public class ProspectorSubscriber {

    public ProspectorSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onProspectedAsteroidEvent(ProspectedAsteroidEvent event) {
        JsonObject params = (JsonObject) SessionTracker.getInstance().getObject("params");

        boolean foundTargetMaterial = false;
        StringBuilder anouncement = new StringBuilder();

        for (ProspectedAsteroidEvent.Material material : event.getMaterials()) {
            String materialNameLocalised = material == null ? "" : material.getName();
            String targetMaterial = params.get("material").getAsString().toLowerCase();

            if (materialNameLocalised != null && !materialNameLocalised.isEmpty() && materialNameLocalised.toLowerCase().equals(targetMaterial)) {
                foundTargetMaterial = true;

                double proportion = material.getProportion();
                anouncement.append("Prospector detected " + String.format("%.2f%%", proportion)).append(" ").append(material.getName());
                break;
            }
        }

        if(foundTargetMaterial) {
            VoiceNotifier.getInstance().speak(anouncement.toString());
        }

    }

}
