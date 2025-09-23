package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
import elite.intel.gameapi.journal.events.LoadGameEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.DaftSecretarySanitizer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static elite.intel.session.PlayerSession.*;
import static elite.intel.util.StringUtls.capitalizeWords;

public class LoadGameEventSubscriber {

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        playerSession.put(SHIP_FUEL_LEVEL, event.getFuelLevel());

        String inGameName = event.getCommander();
        String alternativeName = ConfigManager.getInstance().getPlayerKey(ConfigManager.PLAYER_ALTERNATIVE_NAME);
        String usePlayerName = capitalizeWords(alternativeName != null || !alternativeName.isEmpty() ? alternativeName : inGameName);
        playerSession.put(PLAYER_NAME, usePlayerName);

        playerSession.put(CURRENT_SHIP, event.getShip());
        playerSession.put(CURRENT_SHIP_NAME, event.getShipName());
        playerSession.put(PERSONAL_CREDITS_AVAILABLE, event.getCredits());
        initValuesFromConfig(playerSession);
        cleanUpRoute(playerSession);
    }

    private void cleanUpRoute(PlayerSession playerSession) {
        List<NavRouteDto> orderedRoute = playerSession.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (!roueSet) {return;}
        if (currentLocation == null) {return;}

        NavRouteDto currentSystemRoute = orderedRoute.stream()
                .filter(dto -> dto.getName().equalsIgnoreCase(currentLocation.getStarName()))
                .findFirst()
                .orElse(null);

        Map<Integer, NavRouteDto> adjustedMap = new LinkedHashMap<>();
        for( NavRouteDto dto : orderedRoute ) {
            if (dto.getLeg() > currentSystemRoute.getLeg() && !dto.getName().equalsIgnoreCase(currentSystemRoute.getName())) {
                adjustedMap.put(dto.getLeg(), dto);
            }
        }

        playerSession.setNavRoute(adjustedMap);
    }

    private static void initValuesFromConfig(PlayerSession playerSession) {
        ConfigManager configManager = ConfigManager.getInstance();
        String mission_statement = configManager.getPlayerKey(ConfigManager.PLAYER_MISSION_STATEMENT);
        playerSession.put(PLAYER_MISSION_STATEMENT, mission_statement);
    }
}
