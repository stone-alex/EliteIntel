package elite.intel.util;

import elite.intel.ai.search.spansh.carrierroute.CarrierJump;
import elite.intel.db.FleetCarrierRoute;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdjustRoute {

    public static Map<Integer, NavRouteDto> adjustRoute(List<NavRouteDto> orderedRoute, String starSystem) {
        if (starSystem == null) {
            return convertToMap(orderedRoute);
        }

        // check if our current location is in the route
        NavRouteDto currentLocationInRoute = orderedRoute.stream()
                .filter(dto -> dto.getName().equalsIgnoreCase(starSystem))
                .findFirst()
                .orElse(null);

        if (currentLocationInRoute == null) {
            // our location is not in the route, return the full route 'as is'
            return convertToMap(orderedRoute);
        } else {
            // our location is in the route, adjust the route to only include legs after our current location
            Map<Integer, NavRouteDto> adjustedMap = new LinkedHashMap<>();
            for (NavRouteDto dto : orderedRoute) {
                if (dto.getLeg() > currentLocationInRoute.getLeg()) {
                    adjustedMap.put(dto.getLeg(), dto);
                }
            }
            return adjustedMap;
        }

    }

    private static Map<Integer, NavRouteDto> convertToMap(List<NavRouteDto> orderedRoute) {
        // return as map leg to nav point
        Map<Integer, NavRouteDto> map = new LinkedHashMap<>();
        for (NavRouteDto dto : orderedRoute) {
            map.put(dto.getLeg(), dto);
        }
        return map;
    }


    public static void adjustFleetCarrierRoute(String starSystem) {
        FleetCarrierRoute route = FleetCarrierRoute.getInstance();
        Map<Integer, CarrierJump> fleetCarrierRoute = route.getFleetCarrierRoute();
        Map<Integer, CarrierJump> adjustedRoute = new HashMap<>();
        for (CarrierJump carrierJump : fleetCarrierRoute.values()) {
            if (!carrierJump.getSystemName().equalsIgnoreCase(starSystem)) {
                adjustedRoute.put(carrierJump.getLeg(), carrierJump);
            }
        }
        route.setFleetCarrierRoute(adjustedRoute);
    }

}
