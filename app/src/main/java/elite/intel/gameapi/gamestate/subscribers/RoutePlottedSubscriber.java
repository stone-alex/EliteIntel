package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.session.PlayerSession;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.util.AdjustRoute;

import java.util.*;

@SuppressWarnings("unused") //registered in SubscriberRegistration
public class RoutePlottedSubscriber {

    @Subscribe
    public void onGameEvent(GameEvents.NavRouteEvent event) {
        int totalJumps = event.getRoute().size();
        PlayerSession playerSession = PlayerSession.getInstance();
        ShipRouteManager shipRoute = ShipRouteManager.getInstance();

        List<GameEvents.NavRouteEvent.RouteEntry> route = event.getRoute();
        Map<Integer, NavRouteDto> routeMap = new LinkedHashMap<>();
        int leg = 0;
        if (!route.isEmpty()) {
            for (GameEvents.NavRouteEvent.RouteEntry entry : route.subList(1, route.size())) {
                NavRouteDto dto = new NavRouteDto();
                dto.setLeg(++leg);
                dto.setRemainingJumps(totalJumps - leg);
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
                    playerSession.setFinalDestination(finalDestination.getName());
                }
                if(playerSession.getCurrentLocation() != null) {
                    List<NavRouteDto> orderedRoute = new ArrayList<>(routeMap.values());
                    orderedRoute.sort(Comparator.comparingInt(NavRouteDto::getLeg));
                    Map<Integer, NavRouteDto> adjustedRoute = AdjustRoute.adjustRoute(orderedRoute, playerSession.getCurrentLocation().getStarName());
                    shipRoute.setNavRoute(adjustedRoute);
                }
            } else {
                shipRoute.clearRoute();
            }
        }
    }
}