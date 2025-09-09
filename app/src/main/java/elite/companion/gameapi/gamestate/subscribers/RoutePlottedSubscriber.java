package elite.companion.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.gamestate.events.GameEvents;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.session.PlayerSession;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoutePlottedSubscriber {

    @Subscribe
    public void onGameEvent(GameEvents.NavRouteEvent event) {
        int totalJumps = event.getRoute().size();

        List<GameEvents.NavRouteEvent.RouteEntry> route = event.getRoute();
        Map<String, NavRouteDto> routeMap = new LinkedHashMap<>();
        int leg = 0;
        if (!route.isEmpty()) {
            for (GameEvents.NavRouteEvent.RouteEntry entry : route.subList(1, route.size())) {
                NavRouteDto dto = new NavRouteDto();
                dto.setLeg(leg++);
                dto.setRemainingJumps(totalJumps - leg - 1);
                dto.setStarClass(entry.getStarClass());
                dto.setName(entry.getStarSystem());
                dto.setScoopable("KGBFOAM".contains(entry.getStarClass().toUpperCase()));
                routeMap.put(dto.getName(), dto);
            }

            PlayerSession playerSession = PlayerSession.getInstance();

            NavRouteDto finalDestination;
            if (!routeMap.isEmpty()) {
                finalDestination = routeMap.entrySet().stream()
                        .reduce((first, second) -> second) // Keep the last entry
                        .map(Map.Entry::getValue)
                        .orElse(null);
                if (finalDestination != null) {
                    playerSession.put(PlayerSession.FINAL_DESTINATION, finalDestination.getName());
                }
                playerSession.setNavRoute(routeMap);
            } else {
                playerSession.clearRoute();
            }
        }
    }
}