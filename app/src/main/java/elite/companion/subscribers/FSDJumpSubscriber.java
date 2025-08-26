package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.FSDJumpEvent;
import elite.companion.session.SystemSession;

import java.util.List;

public class FSDJumpSubscriber {

    public FSDJumpSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {

        Float jumpDistance = event.getJumpDist();
        String system = event.getStarSystem();
        String systemAllegiance = event.getSystemAllegiance();
        String economy = event.getSystemEconomyLocalised();
        String government = event.getSystemGovernmentLocalised();
        String security = event.getSystemSecurityLocalised();
        String controllingPower = event.getControllingPower();
        String powerplayState = event.getPowerplayState();
        List<FSDJumpEvent.Faction> factions = event.getFactions();
        StringBuilder factionInfo = new StringBuilder();
        if (factions == null || factions.isEmpty()) {
            for (FSDJumpEvent.Faction faction : factions) {
                factionInfo.append("Faction Name: ");
                factionInfo.append(faction.getName()).append(" ");
                factionInfo.append(", Government: ");
                factionInfo.append(faction.getGovernment());
                ;
                factionInfo.append(", Allegiance: ");
                factionInfo.append(faction.getAllegiance());
                ;
                factionInfo.append(", Influence: ");
                factionInfo.append(faction.getInfluence());
                ;
                factionInfo.append(", Happiness");
                factionInfo.append(faction.getHappinessLocalised());
                factionInfo.append(". ");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("FSD Jump Event: ");
        sb.append("Jump Distance: ").append(jumpDistance).append("ly, ");
        sb.append("System: ").append(system).append(", ");
        sb.append("System Allegiance: ").append(systemAllegiance).append(", ");
        sb.append("Economy: ").append(economy).append(", ");
        sb.append("Government: ").append(government).append(", ");
        sb.append("Security: ").append(security).append(", ");
        sb.append("Controlling Power: ").append(controllingPower).append(", ");
        sb.append("Powerplay State: ").append(powerplayState).append(", ");
        sb.append("Factions: ").append(factionInfo.toString());

        SystemSession.getInstance().setSensorData(sb.toString());

    }
}
