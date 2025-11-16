package elite.intel.session;

import com.google.gson.reflect.TypeToken;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;

import java.util.*;

public class ShipRoute extends SessionPersistence implements java.io.Serializable {

    private static final String DIRECTORY = "session/";
    private static volatile ShipRoute instance;
    private static final String ROUTE_MAP = "routeMap";
    private final Map<Integer, NavRouteDto> routeMap = new LinkedHashMap<>();


    private ShipRoute(String fileName) {
        super(DIRECTORY);
        ensureFileAndDirectoryExist(fileName);

        registerField(ROUTE_MAP, this::getRoute, v -> {
            routeMap.clear();
            routeMap.putAll((Map<Integer, NavRouteDto>) v);
        }, new TypeToken<Map<Integer, NavRouteDto>>() {
        }.getType());

        loadSavedStateFromDisk();
    }

    public static ShipRoute getInstance() {
        if (instance == null) {
            synchronized (ShipRoute.class) {
                if (instance == null) {
                    instance = new ShipRoute("ship_route.json");
                }
            }
        }
        return instance;
    }


    public void setNavRoute(Map<Integer, NavRouteDto> routeMap) {
        this.routeMap.clear();
        this.routeMap.putAll(routeMap);
        save();
    }

    private Map<Integer, NavRouteDto> getRoute() {
        return routeMap;
    }

    public List<NavRouteDto> getOrderedRoute() {
        if (routeMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<NavRouteDto> orderedRoute = new ArrayList<>(routeMap.values());
        orderedRoute.sort(Comparator.comparingInt(NavRouteDto::getLeg));
        return orderedRoute;
    }

    public void clearRoute() {
        routeMap.clear();
        save();
    }

    public void updateRouteNode(NavRouteDto dto) {
        routeMap.put(dto.getLeg(), dto);
        save();
    }

    private void loadSavedStateFromDisk() {
        loadSession(ShipRoute.this::loadFields);
    }

}
