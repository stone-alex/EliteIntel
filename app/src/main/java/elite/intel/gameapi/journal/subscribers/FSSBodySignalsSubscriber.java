package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.commons.BiomeAnalyzer;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.List;

public class FSSBodySignalsSubscriber extends BiomeAnalyzer {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onFssBodySignal(FSSBodySignalsEvent event) {
        LocationDto location = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBodyID());
        location.setPlanetName(event.getBodyName());
        List<FSSBodySignalsEvent.Signal> signals = event.getSignals();
        if (signals == null || signals.isEmpty()) return;

        location.setFssSignals(signals);
        int bioSignals = 0;
        int geoSignals = 0;
        for (FSSBodySignalsEvent.Signal s : signals) {
            FssSignalDto signal = new FssSignalDto();
            signal.setSignalName(event.getEventName());
            signal.setSignalType(s.getTypeLocalised());
            if ("Biological".equalsIgnoreCase(s.getTypeLocalised())) {
                bioSignals = bioSignals + s.getCount();
            }
            if ("Geological".equalsIgnoreCase(s.getTypeLocalised())) {
                geoSignals = geoSignals + s.getCount();
            }
            signal.setSystemAddress(event.getSystemAddress());
            location.addDetectedSignal(signal);
            playerSession.saveLocation(location);
        }

        location.setBioSignals(bioSignals);
        location.setGeoSignals(geoSignals);
        playerSession.saveLocation(location);
    }

}
