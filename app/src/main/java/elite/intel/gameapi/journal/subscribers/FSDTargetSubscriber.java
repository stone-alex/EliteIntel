package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSDTargetEvent;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

@SuppressWarnings("unused")
public class FSDTargetSubscriber {

    @Subscribe
    public void onFSDTargetEvent(FSDTargetEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        StarSystemDto systemDto = EdsmApiClient.searchStarSystem(event.getName(), 1);
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(event.getName());
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(event.getName());

        playerSession.setFsdTarget(
                new DataDto(
                        systemDto,
                        deathsDto,
                        trafficDto,
                        isFuelStarClause(event.getStarClass())
                ).toJson()
        );

    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".toUpperCase().contains(starClass.toUpperCase());
        return isFuelStar ? " (Fuel Star)" : "";
    }

    record DataDto(StarSystemDto systemDto, DeathsDto deathsDto, TrafficDto trafficDto, String fuelStarStatus) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
