package elite.intel.ai.brain.actions.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;

public class AnalyzeDistanceFromFleetCarrierHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        //EventBusManager.publish(new AiVoxResponseEvent("Analyzing travel telemetry. Stand by."));

        CarrierDataDto carrierData = playerSession.getCarrierData();
        if (carrierData == null) return process("No data available");
        double carrierLocationX = carrierData.getX();
        double carrierLocationY = carrierData.getY();
        double carrierDataZ = carrierData.getZ();

        if (carrierLocationX == 0 && carrierLocationY == 0 && carrierDataZ == 0) {
            return process("Carrier coordinates are not available.");
        }

        double x = 0, y = 0, z = 0;
        LocationDto playerLocation = locationManager.findPrimaryStar(playerSession.getPrimaryStarName());
        x = playerLocation.getX();
        y = playerLocation.getY();
        z = playerLocation.getZ();
        boolean arePlayerLocationCoordinatesAvailable = x == 0 && y == 0 && z == 0;
        if (arePlayerLocationCoordinatesAvailable) {
            return process("Player location coordinates are not available.");
        }

        ShipLoadOutDto shipLoadout = playerSession.getShipLoadout();
        double jumpRange = shipLoadout == null ? -1 : shipLoadout.getMaxJumpRange();
        double distance = NavigationUtils.calculateGalacticDistance(x, y, z, carrierLocationX, carrierLocationY, carrierDataZ);

        int numberOfJumps = (int) (distance / jumpRange) + 1;
        StringBuilder sb = new StringBuilder();
        sb.append("Distance is ").append((int) distance).append(" light years. It will take ").append(numberOfJumps).append(" Jump");
        if (numberOfJumps > 1 || numberOfJumps == 0) {
            sb.append("s");
        }
        sb.append(" to get there.");

        return process(sb.toString());
    }
}