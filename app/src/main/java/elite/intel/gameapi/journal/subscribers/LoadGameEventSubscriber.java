package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.LoadGameEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.List;

public class LoadGameEventSubscriber {

    private final ShipRouteManager shipRoute = ShipRouteManager.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onEvent(LoadGameEvent event) {

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
        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        if (!roueSet) {
            return;
        }
        if (currentLocation == null) {
            return;
        }
        shipRoute.removeLeg(currentLocation.getStarName());
    }
}
