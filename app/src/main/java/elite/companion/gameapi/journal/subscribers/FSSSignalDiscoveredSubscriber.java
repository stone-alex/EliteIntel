package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.FSSSignalDiscoveredEvent;
import elite.companion.session.PlayerSession;

@SuppressWarnings("unused")
public class FSSSignalDiscoveredSubscriber {


    @Subscribe
    public void onFSSSignalDiscovered(FSSSignalDiscoveredEvent event) {
        PlayerSession.getInstance().addSignal(event);
        if (event.getUssTypeLocalised().equals("Nonhuman signal source")) {
            EventBusManager.publish(new VoiceProcessEvent("Nonhuman signal source detected! Threat level " + event.getThreatLevel()) + "!");
        }
    }
}
/* { "timestamp":"2025-09-06T21:47:22Z", "event":"FSSSignalDiscovered", "SystemAddress":2965515536771, "SignalName":"$USS_NonHumanSignalSource;", "SignalName_Localised":"Unidentified signal source", "SignalType":"USS", "USSType":"$USS_Type_NonHuman;", "USSType_Localised":"Nonhuman signal source", "SpawningState":"$FactionState_None;", "SpawningState_Localised":"None", "SpawningFaction":"$faction_none;", "SpawningFaction_Localised":"None", "ThreatLevel":7, "TimeRemaining":619.343567 }*/