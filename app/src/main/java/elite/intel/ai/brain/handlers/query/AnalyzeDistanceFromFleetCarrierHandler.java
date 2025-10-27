package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeDistanceFromFleetCarrierHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
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
        LocationDto primarySystem = playerSession.getPrimarySystem();
        x = primarySystem.getX();
        y = primarySystem.getY();
        z = primarySystem.getZ();
        boolean currentLocationCoordinatesAreNotAvailable = x == 0 && y == 0 && z == 0;
        if (currentLocationCoordinatesAreNotAvailable) {
            return process("Current location coordinates are not available.");
        }

        LoadoutEvent shipLoadout = playerSession.getShipLoadout();
        float jumpRange = shipLoadout == null ? -1 : shipLoadout.getMaxJumpRange();
        double distance = NavigationUtils.calculateGalacticDistance(x, y, z, carrierLocationX, carrierLocationY, carrierDataZ);

        String instruction = "Distance is in Light Years. If jump range is > 0 also calculate number of jumps required to reach the carrier. Jump range is in light years. Return whole numbers only, no decimals";
        AiDataStruct struct = new AiDataStruct(instruction, new DataDto(distance, jumpRange, carrierLocation));

        return process(struct, originalUserInput);
    }

    record DataDto(double distance, float jumpRange, String fleetCarrierIsLocatedAt) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}