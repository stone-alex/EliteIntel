package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.List;

import static elite.intel.util.SleepNoThrow.sleep;

public class FSSBodySignalsSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();
    @Subscribe
    public void onFssBodySignal(FSSBodySignalsEvent event) {
        sleep(2000);
        addSignals(event, playerSession.getLocation(event.getBodyID()));
    }

    private void addSignals(FSSBodySignalsEvent event, LocationDto location) {
        List<FSSBodySignalsEvent.Signal> signals = event.getSignals();
        if(signals == null || signals.isEmpty()) return;

        for(FSSBodySignalsEvent.Signal s : signals) {
            FssSignalDto signal = new FssSignalDto();
            signal.setSignalName(event.getEventName());
            signal.setSignalType(s.getTypeLocalised());
            if("Biological".equalsIgnoreCase(s.getTypeLocalised())){
                location.setBioFormsPresent(true);
            }
            signal.setSystemAddress(event.getSystemAddress());
            location.addDetectedSignal(signal);
            playerSession.saveLocation(location);
        }
    }
}
