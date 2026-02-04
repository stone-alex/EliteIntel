package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.commons.BiomeAnalyzer;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.List;

public class FSSBodySignalsSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onFssBodySignal(FSSBodySignalsEvent event) {
        LocationDto primaryStarLocation = locationManager.findPrimaryStar(playerSession.getPrimaryStarName());

        LocationDto location = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBodyID());

        location.setPlanetName(event.getBodyName());
        location.setStarName(primaryStarLocation.getStarName());
        location.setX(primaryStarLocation.getX());
        location.setY(primaryStarLocation.getY());
        location.setZ(primaryStarLocation.getZ());
        List<FSSBodySignalsEvent.Signal> signals = event.getSignals();
        if (signals == null || signals.isEmpty()) return;

        location.setFssSignals(signals);
        int bioSignals = 0;
        int geoSignals = 0;
        for (FSSBodySignalsEvent.Signal s : signals) {
            FssSignalDto signal = new FssSignalDto();
            signal.setSignalName(event.getEventName());
            signal.setSignalType(s.getTypeLocalised());
            if ("$SAA_SignalType_Biological;".equalsIgnoreCase(s.getType())) {
                bioSignals = bioSignals + s.getCount();
            }
            if ("Geological".equalsIgnoreCase(s.getTypeLocalised())) {
                geoSignals = geoSignals + s.getCount();
            }
            signal.setSystemAddress(event.getSystemAddress());
            location.addDetectedSignal(signal);
            locationManager.save(location);
        }

        location.setBioSignals(bioSignals);
        location.setGeoSignals(geoSignals);
        locationManager.save(location);

        if (playerSession.isDiscoveryAnnouncementOn()) {
            if (bioSignals > 0) EventBusManager.publish(new DiscoveryAnnouncementEvent(bioSignals + " bio signals discovered"));
            if (geoSignals > 0) EventBusManager.publish(new DiscoveryAnnouncementEvent(geoSignals + " geo signals discovered"));
        }
    }

}
