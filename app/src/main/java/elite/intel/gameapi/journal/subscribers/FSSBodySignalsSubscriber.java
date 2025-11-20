package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.commons.BiomeAnalyzer;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.SleepNoThrow;

import java.util.List;

public class FSSBodySignalsSubscriber extends BiomeAnalyzer {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onFssBodySignal(FSSBodySignalsEvent event) {
        LocationDto location = playerSession.getLocation(event.getBodyID(), playerSession.getPrimaryStarName());
        List<FSSBodySignalsEvent.Signal> signals = event.getSignals();
        if(signals == null || signals.isEmpty()) return;

        location.setFssSignals(signals);
        int bioSignals = 0;
        int geoSignals = 0;
        for(FSSBodySignalsEvent.Signal s : signals) {
            FssSignalDto signal = new FssSignalDto();
            signal.setSignalName(event.getEventName());
            signal.setSignalType(s.getTypeLocalised());
            if("Biological".equalsIgnoreCase(s.getTypeLocalised())){
                bioSignals++;
            }
            if("Geological".equalsIgnoreCase(s.getTypeLocalised())){
                geoSignals++;
            }
            signal.setSystemAddress(event.getSystemAddress());
            location.addDetectedSignal(signal);
            playerSession.saveLocation(location);
        }
        if(location.getBioSignals() < bioSignals) {
            location.setBioSignals(bioSignals);

        }
        location.setGeoSignals(geoSignals);
        playerSession.saveLocation(location);
        SleepNoThrow.sleep(2000); // wait for the data to be saved, next event might be a detailed scan.
    }

}
