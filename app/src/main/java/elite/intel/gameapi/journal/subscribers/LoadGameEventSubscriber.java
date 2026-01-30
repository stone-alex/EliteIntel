package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.MaterialManager;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.LoadGameEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.EncodedMaterialsDto;
import elite.intel.search.edsm.dto.MaterialsDto;
import elite.intel.search.edsm.dto.MaterialsType;
import elite.intel.session.PlayerSession;

import java.util.List;
import java.util.Map;

public class LoadGameEventSubscriber {

    private final ShipRouteManager shipRoute = ShipRouteManager.getInstance();

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setPlayerName(playerSession.getAlternativeName() == null ? event.getCommander() : playerSession.getAlternativeName());
        playerSession.setInGameName(event.getCommander());
        playerSession.setCurrentShip(event.getShip());
        playerSession.setCurrentShipName(event.getShipName());
        playerSession.setPersonalCreditsAvailable(event.getCredits());
        playerSession.setGameVersion(event.getGameversion());
        playerSession.setGameBuild(event.getBuild());
        cleanUpRoute(playerSession);
    }

    private void cleanUpRoute(PlayerSession playerSession) {
        List<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (!roueSet) {
            return;
        }
        if (currentLocation == null) {
            return;
        }
        shipRoute.removeLeg(currentLocation.getStarName());
    }
}
