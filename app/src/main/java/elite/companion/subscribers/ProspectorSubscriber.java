package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.EventBusManager;
import elite.companion.comms.CommandAction;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.ProspectedAsteroidEvent;
import elite.companion.session.SessionTracker;

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
            String prospectedMaterial = material == null ? "" : material.getName();
            String targetMaterial = String.valueOf(SessionTracker.getInstance().getObject(CommandAction.SET_MINING_TARGET.getParamKey())).replaceAll("\"", "");
            if (prospectedMaterial != null && !prospectedMaterial.isEmpty() && prospectedMaterial.toLowerCase().equals(targetMaterial.toLowerCase())) {
                foundTargetMaterial = true;

                double proportion = material.getProportion();
                anouncement.append("Prospector detected " + String.format("%.2f%%", proportion)).append(" ").append(material.getName());
                break;
            }
        }

        if(foundTargetMaterial) {
            VoiceGenerator.getInstance().speak(anouncement.toString());
        }

    }

}
