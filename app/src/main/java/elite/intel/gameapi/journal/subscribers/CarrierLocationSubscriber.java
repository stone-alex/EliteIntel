package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.StarSystemDto;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.session.PlayerSession;

public class CarrierLocationSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {
        Thread.ofVirtual().start(() -> {
            if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())
                    || "SquadronCarrier".equalsIgnoreCase(event.getCarrierType())) {

                boolean isSquadron = "SquadronCarrier".equalsIgnoreCase(event.getCarrierType());

                if (!isSquadron) {
                    playerSession.setLastKnownCarrierLocation(event.getStarSystem());
                }
                FleetCarrierRouteManager route = FleetCarrierRouteManager.getInstance();
                CarrierDataDto carrierData = isSquadron ? new CarrierDataDto() : playerSession.getCarrierData();
                carrierData.setStarName(event.getStarSystem());

                CarrierJump currentCompletedJump = route.findByPrimaryStar(event.getStarSystem());
                boolean routeEntryFound = false;
                if (currentCompletedJump != null) {
                    carrierData.setX(currentCompletedJump.getX());
                    carrierData.setY(currentCompletedJump.getY());
                    carrierData.setZ(currentCompletedJump.getZ());
                    if (isSquadron) {
                        playerSession.setSquadronCarrierData(carrierData);
                    } else {
                        playerSession.setCarrierData(carrierData);
                    }

                    routeEntryFound = true;
                }

                route.removeLeg(event.getStarSystem());

                if (!routeEntryFound) {
                    final CarrierDataDto finalCarrierData = carrierData;
                    Thread.ofVirtual().start(() -> {
                        // try via EDSM
                        StarSystemDto starSystemDto = EdsmApiClient.searchStarSystem(event.getStarSystem(), 1);
                        StarSystemDto.Coords coords = starSystemDto.getCoords();
                        boolean isSol = starSystemDto.getData() != null
                                && "sol".equalsIgnoreCase(starSystemDto.getData().getName());
                        boolean hasValidCoords = coords != null
                                && (isSol || coords.getX() != 0 || coords.getY() != 0 || coords.getZ() != 0);
                        if (hasValidCoords) {
                            finalCarrierData.setX(coords.getX());
                            finalCarrierData.setY(coords.getY());
                            finalCarrierData.setZ(coords.getZ());
                            if (isSquadron) {
                                playerSession.setSquadronCarrierData(finalCarrierData);
                            } else {
                                playerSession.setCarrierData(finalCarrierData);
                            }
                        } else {
                            // try via saved locations
                            LocationManager locationData = LocationManager.getInstance();
                            LocationDto location = locationData.findPrimaryStar(event.getStarSystem());
                            finalCarrierData.setX(location.getX());
                            finalCarrierData.setY(location.getY());
                            finalCarrierData.setZ(location.getZ());
                            if (isSquadron) {
                                playerSession.setSquadronCarrierData(finalCarrierData);
                            } else {
                                playerSession.setCarrierData(finalCarrierData);
                            }
                        }
                    });
                }
            }
        });
    }
}
