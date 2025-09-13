package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.api.EdsmApiClient;
import elite.intel.ai.search.api.dto.DeathsDto;
import elite.intel.ai.search.api.dto.StarSystemDto;
import elite.intel.ai.search.api.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSDTargetEvent;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;

@SuppressWarnings("unused")
public class FSDTargetSubscriber {

    @Subscribe
    public void onFSDTargetEvent(FSDTargetEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String fsdTarget = event.getName() + isFuelStarClause(event.getStarClass());
        EventBusManager.publish(new AppLogEvent("Processing FSDTargetEvent. Storing in session only: " + fsdTarget));

        StarSystemDto systemDto = EdsmApiClient.searchStarSystem(event.getName(), 1);
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(event.getName());
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(event.getName());
        StringBuilder sb = new StringBuilder();


        if (systemDto.getData() != null) {
            sb.append(systemDto.getData().toString()).append(", ");
            sb.append(deathsDto.getData().toString()).append(", ");
            sb.append(trafficDto.getData().toString());
            playerSession.put(PlayerSession.FSD_TARGET, sb.toString());
        } else {
            playerSession.put(PlayerSession.FSD_TARGET, fsdTarget);
        }

    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".toUpperCase().contains(starClass.toUpperCase());
        return isFuelStar ? " (Fuel Star)" : "";
    }
}
