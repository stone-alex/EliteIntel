package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.dto.DeathsDto;
import elite.companion.ai.search.api.dto.StarSystemDto;
import elite.companion.ai.search.api.dto.TrafficDto;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.journal.events.FSDTargetEvent;
import elite.companion.session.PlayerSession;
import elite.companion.ui.event.AppLogEvent;

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
