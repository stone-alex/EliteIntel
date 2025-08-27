package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.FSDJumpEvent;
import elite.companion.session.SystemSession;

import java.util.List;

public class FSDJumpSubscriber {

    public FSDJumpSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {

        String currentStarSystem = event.getStarSystem();
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

        SystemSession systemSession = SystemSession.getInstance();
        systemSession.updateSession(systemSession.CURRENT_SYSTEM, event.getStarSystem());
        boolean roueSet = !systemSession.getRoute().isEmpty();
        systemSession.removeNavPoint(currentStarSystem);
        String finalDestination = String.valueOf(systemSession.getObject(SystemSession.FINAL_DESTINATION));

        StringBuilder sb = new StringBuilder();
        sb.append("FSD Jump Complete: ");

        sb.append("System name: ").append(event.getStarSystem()).append(", ");
        sb.append("System Allegiance: ").append(systemAllegiance).append(", ");
        sb.append("Government: ").append(government).append(", ");
        sb.append("Security: ").append(security).append(", ");
        sb.append("Controlling Power: ").append(controllingPower).append(", ");
        sb.append("Powerplay State: ").append(powerplayState).append(", ");
        sb.append("Economy: ").append(economy).append(", ");

        if (finalDestination != null && finalDestination.equalsIgnoreCase(currentStarSystem)) {
            sb.append("Arrived at final destination: ").append(finalDestination).append(" true, ");
        } else {

            if (roueSet) {
                int remainingJump = systemSession.getRoute().size();

                if (remainingJump > 0) {
                    sb.append("Next Stop: ").append(systemSession.getObject(systemSession.FSD_TARGET)).append(", ");
                }

                sb.append("Jumps remaining to final destination: ").append(remainingJump).append(finalDestination).append(",");
            }
        }

        systemSession.setSensorData(sb.toString());
    }
}
