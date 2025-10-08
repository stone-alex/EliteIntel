package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.LoadGameEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.AdjustRoute;

import java.util.List;
import java.util.Map;

import static elite.intel.util.StringUtls.capitalizeWords;

public class LoadGameEventSubscriber {

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        playerSession.setShipFuelLevel(event.getFuelLevel());

        String inGameName = event.getCommander();
        String alternativeName = ConfigManager.getInstance().getPlayerKey(ConfigManager.PLAYER_ALTERNATIVE_NAME);
        String usePlayerName = capitalizeWords(alternativeName != null || !alternativeName.isEmpty() ? alternativeName : inGameName);
        playerSession.setPlayerName(usePlayerName);

        playerSession.setCurrentShip(event.getShip());
        playerSession.setCurrentShipName(event.getShipName());
        playerSession.setPersonalCreditsAvailable(event.getCredits());
        initValuesFromConfig(playerSession);
        cleanUpRoute(playerSession);
    }

    private void cleanUpRoute(PlayerSession playerSession) {
        List<NavRouteDto> orderedRoute = playerSession.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (!roueSet) {return;}
        if (currentLocation == null) {return;}

        Map<Integer, NavRouteDto> adjustedRoute = AdjustRoute.adjustRoute(orderedRoute, currentLocation.getStarName());
        playerSession.setNavRoute(adjustedRoute);
    }

    private static void initValuesFromConfig(PlayerSession playerSession) {
        ConfigManager configManager = ConfigManager.getInstance();
        String mission_statement = configManager.getPlayerKey(ConfigManager.PLAYER_MISSION_STATEMENT);
        playerSession.setPlayerMissionStatement(mission_statement);
    }
}
