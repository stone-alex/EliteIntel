package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeDistanceFromFleetCarrierHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing travel telemetry... Stand by..."));

        CarrierDataDto carrierData = playerSession.getCarrierData();
        if (carrierData == null) return process("No data available");
        String carrierLocation = playerSession.getLastKnownCarrierLocation();
        double carrierLocationX = carrierData.getX();
        double carrierLocationY = carrierData.getY();
        double carrierDataZ = carrierData.getZ();

        if (carrierLocationX == 0 && carrierLocationY == 0 && carrierDataZ == 0) {
            return process("Carrier coordinates are not available.");
        }

        double x = 0, y = 0, z = 0;
        LocationDto primarySystem = locationManager.findPrimaryStar(playerSession.getPrimaryStarName());
        x = primarySystem.getX();
        y = primarySystem.getY();
        z = primarySystem.getZ();
        boolean currentLocationCoordinatesAreNotAvailable = x == 0 && y == 0 && z == 0;
        if (currentLocationCoordinatesAreNotAvailable) {
            return process("Current location coordinates are not available.");
        }

        ShipLoadOutDto shipLoadout = playerSession.getShipLoadout();
        double jumpRange = shipLoadout == null ? -1 : shipLoadout.getMaxJumpRange();
        double distance = NavigationUtils.calculateGalacticDistance(x, y, z, carrierLocationX, carrierLocationY, carrierDataZ);

        int numberOfJumps = (int) (distance / jumpRange);
        return process("Distance is " + (int) distance + " it will take " + numberOfJumps + " Jumps to get there.");
    }
}