package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SAAScanCompleteEvent;
import elite.intel.session.PlayerSession;

public class SAAScanCompleteSubscriber {

    @Subscribe
    public void onSAAScanComplete(SAAScanCompleteEvent event) {

        if (PlayerSession.getInstance().isDiscoveryAnnouncementOn()) {
            String message = "Surface scan complete " + efficiency(event);
            EventBusManager.publish(new SensorDataEvent(message, "Notify User"));
        }
    }

    private String efficiency(SAAScanCompleteEvent event) {
        if (event.getEfficiencyTarget() > event.getProbesUsed()) {
            return " Efficiency Excellent: Only " + event.getProbesUsed() + " probes used out of " + event.getEfficiencyTarget() + ". ";
        } else if (event.getEfficiencyTarget() == event.getProbesUsed() || event.getBodyName().contains("Ring")) {
            return " Efficiency Target Met. ";
        } else {
            return " Efficiency Poor: " + event.getProbesUsed() + " exceeding efficiency target of " + event.getEfficiencyTarget() + " probes. ";
        }
    }
}
