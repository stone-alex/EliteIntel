package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
import elite.intel.session.PlayerSession;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused") //registered in SubscriberRegistration
public class RoutePlottedSubscriber {

    @Subscribe
    public void onGameEvent(GameEvents.NavRouteEvent event) {
        int totalJumps = event.getRoute().size();
        PlayerSession playerSession = PlayerSession.getInstance();

        List<GameEvents.NavRouteEvent.RouteEntry> route = event.getRoute();
        Map<Integer, NavRouteDto> routeMap = new LinkedHashMap<>();
        int leg = 0;
        if (!route.isEmpty()) {
            for (GameEvents.NavRouteEvent.RouteEntry entry : route.subList(1, route.size())) {
                NavRouteDto dto = new NavRouteDto();
                dto.setLeg(++leg);
                dto.setRemainingJumps(totalJumps - leg - 1);
                dto.setStarClass(entry.getStarClass());
                dto.setName(entry.getStarSystem());
                dto.setX(entry.getStarPos()[0]);
                dto.setY(entry.getStarPos()[1]);
                dto.setZ(entry.getStarPos()[2]);
                dto.setScoopable("KGBFOAM".contains(entry.getStarClass().toUpperCase()));
                routeMap.put(leg, dto);
            }

            NavRouteDto finalDestination;
            if (!routeMap.isEmpty()) {
                finalDestination = routeMap.entrySet().stream()
                        .reduce((first, second) -> second) // Keep the last entry
                        .map(Map.Entry::getValue)
                        .orElse(null);
                if (finalDestination != null) {
                    playerSession.put(PlayerSession.FINAL_DESTINATION, finalDestination.getName());
                }
                if(playerSession.getCurrentLocation() != null) {
                    for (NavRouteDto navRouteDto : routeMap.values()) {
                        if (navRouteDto.getName().equalsIgnoreCase(playerSession.getCurrentLocation().getStarName())) {
                            routeMap.remove(navRouteDto.getLeg());
                        }
                    }
                }
                playerSession.setNavRoute(routeMap);
            } else {
                playerSession.clearRoute();
            }
        }
    }
}