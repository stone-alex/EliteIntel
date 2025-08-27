package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import elite.companion.EventBusManager;
import elite.companion.comms.handlers.command.CommandAction;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.events.ProspectedAsteroidEvent;
import elite.companion.session.PlayerSession;

public class ProspectorSubscriber {

    public ProspectorSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onProspectedAsteroidEvent(ProspectedAsteroidEvent event) {
        JsonObject params = (JsonObject) PlayerSession.getInstance().getObject("params");

        boolean foundTargetMaterial = false;
        StringBuilder anouncement = new StringBuilder();

        for (ProspectedAsteroidEvent.Material material : event.getMaterials()) {
            String prospectedMaterial = material == null ? "" : material.getName();
            String targetMaterial = String.valueOf(PlayerSession.getInstance().getObject(CommandAction.SET_MINING_TARGET.getParamKey())).replaceAll("\"", "");
            if (prospectedMaterial != null && !prospectedMaterial.isEmpty() && prospectedMaterial.toLowerCase().equals(targetMaterial.toLowerCase())) {
                foundTargetMaterial = true;

                double proportion = material.getProportion();
                String message = "Prospector detected " + String.format("%.2f", proportion) + " percent " + material.getName();
                anouncement.append(message);

                break;
            }
        }

        if(foundTargetMaterial) {
            VoiceGenerator.getInstance().speak(anouncement.toString());
        }

    }

}
