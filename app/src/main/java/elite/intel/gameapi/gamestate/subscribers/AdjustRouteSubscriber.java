package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.FSDTargetEvent;

import java.util.List;

public class AdjustRouteSubscriber {
    private final ShipRouteManager shipRouteManager = ShipRouteManager.getInstance();

    @Subscribe
    public void onFsdTargetSelected(FSDTargetEvent event) {

        List<NavRouteDto> orderedRoute = shipRouteManager.getOrderedRoute();
        if (orderedRoute.isEmpty()) return;

        String ourNextJump = event.getName();

        if (ourNextJump == null || ourNextJump.isBlank()) return;
        if (orderedRoute.getFirst().getName().equalsIgnoreCase(ourNextJump)) return;

        /// route info is out of sync. trim or clear.
        for (NavRouteDto leg : orderedRoute) {
            if (leg.getName().equalsIgnoreCase(ourNextJump)) {
                break;
            }
            shipRouteManager.removeLeg(leg.getName());
        }
    }
}
