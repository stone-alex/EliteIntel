package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.CarrierJumpRequestEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.TimestampFormatter;

public class CarrierJumpRequestSubscriber {

    public CarrierJumpRequestSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onCarrierJumpRequestEvent(CarrierJumpRequestEvent event) {

        String fleetCarrierType = event.getCarrierType(); //nulls
        String destinationSystemName = event.getSystemName(); //nulls
        String destinationStellarBody = event.getBody();
        String departureTime = TimestampFormatter.formatTimestamp(event.getDepartureTime(), true);

        StringBuilder sb = new StringBuilder();
        sb.append("Carrier Jump Scheduled: ");
        sb.append(" to ");
        sb.append(destinationStellarBody);
        sb.append(" departure time: ");
        sb.append(departureTime);
        sb.append(".");


        //Carrier Jump Scheduled: null to null at Synuefe XR-H d11-124 A on unknown
        SystemSession.getInstance().setSensorData(sb.toString());
    }
}
