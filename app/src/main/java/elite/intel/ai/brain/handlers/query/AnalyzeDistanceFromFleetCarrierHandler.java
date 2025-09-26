package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeDistanceFromFleetCarrierHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        LoadoutEvent shipLoadout = playerSession.getShipLoadout();
        if(carrierData == null) return analyzeData(toJson("No data available"), originalUserInput);
        if(currentLocation == null) return analyzeData(toJson("No data available"), originalUserInput);

        float jumpRange = shipLoadout == null ? -1 : shipLoadout.getMaxJumpRange();

        double carrier_location_x = carrierData.getX();
        double carrier_location_y = carrierData.getY();
        double carrier_location_z = carrierData.getZ();

        double current_location_x = currentLocation.getX();
        double current_location_y = currentLocation.getY();
        double current_location_z = currentLocation.getZ();

        if(carrier_location_x == 0 && carrier_location_y == 0 && carrier_location_z == 0){
            return analyzeData(toJson("Carrier coordinates are not available."), originalUserInput);
        }

        if(current_location_x == 0 && current_location_y == 0 && current_location_z == 0){
            return analyzeData(toJson("Current location coordinates are not available."), originalUserInput);
        }

        String instruction = " Use galactic coordinates to calculate distance from our location to the carrier. If jump range is > 0 also calculate number of jumps required to reach the carrier. ";
        return analyzeData(
                new DataDto(
                        carrier_location_x,
                        carrier_location_y,
                        carrier_location_z,
                        current_location_x,
                        current_location_y,
                        current_location_z,
                        jumpRange,
                        instruction
                ).toJson(),
                originalUserInput
        );
    }

    record DataDto(double carrier_location_x, double carrier_location_y, double carrier_location_z, double our_location_x, double our_location_y, double our_location_z, float jumpRange, String instruction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}