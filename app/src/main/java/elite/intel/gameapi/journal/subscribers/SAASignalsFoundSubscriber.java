package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;

import java.util.List;

public class SAASignalsFoundSubscriber {

    @Subscribe
    public void onSAASignalsFound(SAASignalsFoundEvent event) {
        StringBuilder sb = new StringBuilder();
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.addSaaSignals(event.getSignals());
        playerSession.saveCurrentLocation(currentLocation);

        List<SAASignalsFoundEvent.Signal> signals = event.getSignals();
        int signalsFound = signals != null ? signals.size() : 0;

        String bodyName = event.getBodyName();
        StellarObjectDto stellarObjectDto = playerSession.getStellarObjects().get(bodyName);
        if(stellarObjectDto == null) {return;}

        if (signalsFound > 0) {
            int liveSignals = event.getGenuses() != null ? event.getGenuses().size() : 0;
            sb.append(" Signal(s) found: ");
            for (SAASignalsFoundEvent.Signal signal : signals) {
                sb.append(" Type: ").append(signal.getType()).append(". ");
                if ("Tritium".equals(signal.getType())) {
                    sb.append(" Carrier fuel source is detected. ");
                }
            }



            if (liveSignals > 0) {
                stellarObjectDto.setNumberOfBioFormsPresent(liveSignals);
                stellarObjectDto.setGenus(event.getGenuses());
                playerSession.addStellarObject(stellarObjectDto);
                currentLocation.setGenus(event.getGenuses());
                playerSession.saveCurrentLocation(currentLocation);

                sb.append(" Exobiology signal(s) found ").append(liveSignals).append(": ");
                long averageProjectedPayment = 0;
                for (SAASignalsFoundEvent.Genus genus : event.getGenuses()) {
                    averageProjectedPayment = averageProjectedPayment + BioForms.getAverageProjectedPayment(genus.getGenus());
                    sb.append(" ");
                    sb.append(genus.getGenusLocalised());
                    sb.append(", ");
                }
                sb.append("Average projected payment: ").append(averageProjectedPayment).append(" credits. Plus bonus if first discovered.");

            }else if(bodyName.contains("Ring")){
                //Rings are bodies
                StellarObjectDto ring = new StellarObjectDto();
                ring.setBodyId(event.getBodyID());
                ring.setName(bodyName);
                String parentBodyName = bodyName.substring(0, bodyName.length() - " X Ring".length());
                StellarObjectDto parent = playerSession.getStellarObject(parentBodyName);
                parent.setHasRings(true);
                if(event.getSignals() != null) {
                    ring.setSaasSignals(event.getSignals());
                    ring.setGeoSignals(event.getSignals().size());
                    parent.setSaasSignals(event.getSignals());
                }
                playerSession.addStellarObject(ring);
                playerSession.addStellarObject(parent);
            }

            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        } else {
            EventBusManager.publish(new SensorDataEvent("No Signal(s) detected."));
        }
    }
}
