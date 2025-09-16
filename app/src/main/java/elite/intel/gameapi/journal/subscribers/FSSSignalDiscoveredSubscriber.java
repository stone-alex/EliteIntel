package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.FSSSignalDiscoveredEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.TimeUtils;

@SuppressWarnings("unused")
public class FSSSignalDiscoveredSubscriber {

    private static final String USS_TYPE_SALVAGE = "$USS_Type_Salvage";
    private static final String USS_TYPE_VALUABLE_SALVAGE = "$USS_Type_ValuableSalvage";
    private static final String USS_TYPE_VERY_VALUABLE_SALVAGE = "$USS_Type_VeryValuableSalvage";

    @Subscribe
    public void onFSSSignalDiscovered(FSSSignalDiscoveredEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.addSignal(event);
        playerSession.put(PlayerSession.LAST_SCAN, event.toJson());

        if (event.getUssTypeLocalised() != null && event.getUssTypeLocalised().equals("Nonhuman signal source")) {
            publishVoice("Nonhuman signal source detected! Threat level " + event.getThreatLevel() + "!");
        }
        if (event.getUssType() != null && event.getUssType().contains(USS_TYPE_SALVAGE)) {
            announceSalvage("Low quality salvage", event);
        }
        if (event.getUssType() != null && event.getUssType().contains(USS_TYPE_VALUABLE_SALVAGE)) {
            announceSalvage("Valuable salvage", event);
        }
        if (event.getUssType() != null && event.getUssType().contains(USS_TYPE_VERY_VALUABLE_SALVAGE)) {
            announceSalvage("Very Valuable salvage", event);
        }
    }

    private void publishVoice(String message) {
        EventBusManager.publish(new VoiceProcessEvent(message));
    }


    private void announceSalvage(String qualityLabel, FSSSignalDiscoveredEvent event) {
        StringBuilder msg = new StringBuilder()
                .append(qualityLabel).append(" ")
                .append(event.getUssTypeLocalised()).append(": ")
                .append(TimeUtils.secondsToMinutesRemainingString(event.getTimeRemaining()));

        if (event.getThreatLevel() > 0) {
            msg.append(", threat level: ").append(event.getThreatLevel());
        }

        publishVoice(msg.toString());
    }

}