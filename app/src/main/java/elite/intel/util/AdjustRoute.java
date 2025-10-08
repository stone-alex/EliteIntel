package elite.intel.util;

import elite.intel.ai.search.spansh.carrier.CarrierJump;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdjustRoute {

    public static Map<Integer, NavRouteDto> adjustRoute(List<NavRouteDto> orderedRoute) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (currentLocation == null) {
            return convertToMap(orderedRoute);
        }

        // check if our current location is in the route
        NavRouteDto currentLocationInRoute = orderedRoute.stream()
                .filter(dto -> dto.getName().equalsIgnoreCase(currentLocation.getStarName()))
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
        PlayerSession playerSession = PlayerSession.getInstance();
        Map<Integer, CarrierJump> fleetCarrierRoute = playerSession.getFleetCarrierRoute();
        Map<Integer, CarrierJump> adjustedRoute = new HashMap<>();
        for(CarrierJump carrierJump : fleetCarrierRoute.values()){
            if(carrierJump.getSystemName().equalsIgnoreCase(starSystem)){ continue ;}
            else {
                adjustedRoute.put(carrierJump.getLeg(), carrierJump);
            }
        }
        playerSession.setFleetCarrierRoute(adjustedRoute);
    }

}
