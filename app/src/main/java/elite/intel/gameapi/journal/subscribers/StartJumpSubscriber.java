package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.StartJumpEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import static elite.intel.util.StringUtls.isFuelStarClause;

@SuppressWarnings("unused")
public class StartJumpSubscriber {

    @Subscribe
    public void onStartJumpEvent(StartJumpEvent event) {
        String jumpingTo = event.getStarSystem();
        String starClass = event.getStarClass();
        boolean scoopable = event.isScoopable();

        StringBuilder sb = new StringBuilder();
        sb.append("Traveling through hyperspace on route to ");
        sb.append(jumpingTo);
        sb.append(", ");
        sb.append("Star Class: ");
        sb.append(starClass);
        sb.append(", ");
        sb.append(isFuelStarClause(starClass));
        sb.append(". ");

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.JUMPING_TO, jumpingTo);


        //clear last location data
        //if ("Hyperspace".equalsIgnoreCase(event.getJumpType())) {
            playerSession.saveCurrentLocation(new LocationDto());
            playerSession.clearMiningTargets();
            playerSession.clearStellarObjectsAndSignals();
            playerSession.setLastScan(null);
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        //}

    }

}
