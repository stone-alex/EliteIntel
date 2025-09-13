package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SAAScanCompleteEvent;

public class SAAScanCompleteSubscriber {

    @Subscribe
    public void onSAAScanComplete(SAAScanCompleteEvent event) {

        StringBuilder sb = new StringBuilder();
        sb.append("Surface scan complete ");
        sb.append(efficiency(event));

        EventBusManager.publish(new SensorDataEvent(sb.toString()));

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
