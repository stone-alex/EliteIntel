package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.EncodedMaterialsDto;
import elite.intel.ai.search.edsm.dto.MaterialsDto;
import elite.intel.ai.search.edsm.dto.MaterialsType;
import elite.intel.db.util.Database;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.gameapi.data.EDMaterialCaps;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.LoadGameEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.util.AdjustRoute;

import java.util.List;
import java.util.Map;

public class LoadGameEventSubscriber {

    private final ShipRouteManager shipRoute = ShipRouteManager.getInstance();


    @Subscribe
    public void onEvent(LoadGameEvent event) {

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setShipFuelLevel(event.getFuelLevel());
        playerSession.setPlayerName(event.getCommander());
        playerSession.setCurrentShip(event.getShip());
        playerSession.setCurrentShipName(event.getShipName());
        playerSession.setPersonalCreditsAvailable(event.getCredits());
        playerSession.setGameVersion(event.getGameversion());
        cleanUpRoute(playerSession);

        retrieveManufacturedAndRawMaterialsFromEDSM();
        retrieveEncodedMaterialsFromEDSM();
    }

    private static void retrieveEncodedMaterialsFromEDSM() {
        EncodedMaterialsDto encodedMaterials = EdsmApiClient.getEncodedMaterials();
        for (EncodedMaterialsDto.EncodedMaterialEntry entry : encodedMaterials.getEncoded()) {
            Database.withDao(MaterialsDao.class, dao -> {
                dao.upsert(
                        entry.getMaterialName(),
                        MaterialsType.GAME_ENCODED.getType(),
                        entry.getQuantity(),
                        EDMaterialCaps.getMax(entry.getMaterialName())
                );
                return null;
            });
        }
    }

    private static void retrieveManufacturedAndRawMaterialsFromEDSM() {
        MaterialsDto rawAndManufacturedMaterials = EdsmApiClient.getMaterials();
        for (MaterialsDto.MaterialEntry entry : rawAndManufacturedMaterials.getMaterials()) {
            Database.withDao(MaterialsDao.class, dao -> {
                dao.upsert(
                        entry.getMaterialName(),
                        MaterialsType.GAME_RAW.name(),
                        entry.getQuantity(),
                        EDMaterialCaps.getMax(entry.getMaterialName())
                );
                return null;
            });
        }
    }

    private void cleanUpRoute(PlayerSession playerSession) {
        List<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (!roueSet) {return;}
        if (currentLocation == null) {return;}

        Map<Integer, NavRouteDto> adjustedRoute = AdjustRoute.adjustRoute(orderedRoute, currentLocation.getStarName());
        shipRoute.setNavRoute(adjustedRoute);
    }
}
