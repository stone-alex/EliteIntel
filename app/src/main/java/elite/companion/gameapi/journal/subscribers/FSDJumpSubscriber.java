package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.FSDJumpEvent;
import elite.companion.session.SystemSession;

import java.util.List;

@SuppressWarnings("unused")
public class FSDJumpSubscriber {

    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.updateSession(SystemSession.CURRENT_SYSTEM, event.getStarSystem());
        systemSession.updateSession(SystemSession.CURRENT_SYSTEM_DATA, event.toJson());


        String currentStarSystem = event.getStarSystem();
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


        boolean roueSet = !systemSession.getRoute().isEmpty();
        systemSession.removeNavPoint(currentStarSystem);
        String finalDestination = String.valueOf(systemSession.getObject(SystemSession.FINAL_DESTINATION));

        StringBuilder sb = new StringBuilder();
        sb.append("Hyperspace Jump Successful: ");

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

        systemSession.setConsumableData(sb.toString());
    }
}
