package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Map;

public class AnalyzeDistanceFromFleetCarrierHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();
        String carrierLocation = playerSession.getLastKnownCarrierLocation();
        double x = 0, y = 0, z = 0;

        Map<Long, LocationDto> locations = playerSession.getLocations();
        for (LocationDto location : locations.values()) {
            if (location.getLocationType().equals(LocationDto.LocationType.PRIMARY_STAR)) {
                x = location.getX();
                y = location.getY();
                z = location.getZ();
                break;
            }
        }


        LoadoutEvent shipLoadout = playerSession.getShipLoadout();
        if(carrierData == null) return analyzeData(toJson("No data available"), originalUserInput);
        boolean currentLocationCoordinatesAreNotAvailable = x == 0 && y == 0 && z == 0;

        float jumpRange = shipLoadout == null ? -1 : shipLoadout.getMaxJumpRange();

        double carrier_location_x = carrierData.getX();
        double carrier_location_y = carrierData.getY();
        double carrier_location_z = carrierData.getZ();

        if(carrier_location_x == 0 && carrier_location_y == 0 && carrier_location_z == 0){
            return analyzeData(toJson("Carrier coordinates are not available."), originalUserInput);
        }

        if (currentLocationCoordinatesAreNotAvailable) {
            return analyzeData(toJson("Current location coordinates are not available."), originalUserInput);
        }

        double distance = NavigationUtils.calculateGalacticDistance(x, y, z, carrier_location_x, carrier_location_y, carrier_location_z);

        String instruction = "Distance is in Light Years. If jump range is > 0 also calculate number of jumps required to reach the carrier as distance divided by jump range. Jump range is in light years.";
        return analyzeData(
                new DataDto(
                        distance,
                        jumpRange,
                        carrierLocation,
                        instruction
                ).toJson(),
                originalUserInput
        );
    }

    record DataDto(double distance, float jumpRange, String fleetCarrierIsLocatedAt, String instruction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}