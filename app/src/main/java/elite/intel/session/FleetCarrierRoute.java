package elite.intel.session;

import com.google.gson.reflect.TypeToken;
import elite.intel.ai.search.spansh.carrierroute.CarrierJump;

import java.util.HashMap;
import java.util.Map;

public class FleetCarrierRoute extends SessionPersistence implements java.io.Serializable {

    private static final String DIRECTORY = "session/";
    private static volatile FleetCarrierRoute instance;
    private static final String ROUTE_MAP = "routeMap";
    private Map<Integer, CarrierJump> fleetCarrierRoute = new HashMap<>();

    private FleetCarrierRoute(String fileName) {
        super(DIRECTORY);
        ensureFileAndDirectoryExist(fileName);

        registerField(ROUTE_MAP, this::getFleetCarrierRoute, v -> {
            fleetCarrierRoute.clear();
            fleetCarrierRoute.putAll(v);
        }, new TypeToken<Map<Integer, CarrierJump>>() {
        }.getType());

        loadFromDisk();
    }


    public void loadFromDisk() {
        loadSession(FleetCarrierRoute.this::loadFields);
    }


    public static FleetCarrierRoute getInstance() {
        if (instance == null) {
            synchronized (FleetCarrierRoute.class) {
                if (instance == null) {
                    instance = new FleetCarrierRoute("fleet_carrier_route.json");
                }
            }
        }
        return instance;
    }



    public void setFleetCarrierRoute(Map<Integer, CarrierJump> fleetCarrierRoute) {
        this.fleetCarrierRoute = fleetCarrierRoute;
        save();
    }

    public Map<Integer, CarrierJump> getFleetCarrierRoute() {
        return fleetCarrierRoute;
    }
}
