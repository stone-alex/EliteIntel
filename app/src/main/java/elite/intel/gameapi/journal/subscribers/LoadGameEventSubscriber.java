package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.LoadGameEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.ShipRoute;
import elite.intel.util.AdjustRoute;

import java.util.List;
import java.util.Map;

import static elite.intel.util.StringUtls.capitalizeWords;

public class LoadGameEventSubscriber {

    private final ShipRoute shipRoute = ShipRoute.getInstance();


    @Subscribe
    public void onEvent(LoadGameEvent event) {

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setShipFuelLevel(event.getFuelLevel());
        String nikName = ConfigManager.getInstance().getPlayerKey(ConfigManager.PLAYER_ALTERNATIVE_NAME);
        String usePlayerName = capitalizeWords(nikName != null || !nikName.isEmpty() ? nikName : "Commander");
        playerSession.setPlayerName(usePlayerName);

        playerSession.setCurrentShip(event.getShip());
        playerSession.setCurrentShipName(event.getShipName());
        playerSession.setPersonalCreditsAvailable(event.getCredits());
        initValuesFromConfig(playerSession);
        cleanUpRoute(playerSession);
    }

    private void cleanUpRoute(PlayerSession playerSession) {
        List<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (!roueSet) {return;}
        if (currentLocation == null) {return;}

        Map<Integer, NavRouteDto> adjustedRoute = AdjustRoute.adjustRoute(orderedRoute, currentLocation.getStarName());
        shipRoute.setNavRoute(adjustedRoute);
    }

    private static void initValuesFromConfig(PlayerSession playerSession) {
        ConfigManager configManager = ConfigManager.getInstance();
        String mission_statement = configManager.getPlayerKey(ConfigManager.PLAYER_MISSION_STATEMENT);
        playerSession.setPlayerMissionStatement(mission_statement);
    }
}
