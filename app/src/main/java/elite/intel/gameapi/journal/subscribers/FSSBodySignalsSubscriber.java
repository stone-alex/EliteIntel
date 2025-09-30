package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;

public class FSSBodySignalsSubscriber {

    @Subscribe
    public void onFssBodySignal(FSSBodySignalsEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto location = playerSession.getLocation(event.getBodyID());
        location.addDetectedSignals(toFssSignalDto(event));
    }

    private List<FssSignalDto> toFssSignalDto(FSSBodySignalsEvent event) {
        List<FSSBodySignalsEvent.Signal> signals = event.getSignals();
        if(signals == null || signals.isEmpty()) {return new ArrayList<>();}

        ArrayList<FssSignalDto> result = new ArrayList<>(signals.size());
        for(FSSBodySignalsEvent.Signal s : signals) {
            FssSignalDto signal = new FssSignalDto();
            signal.setSignalName(event.getEventName());
            signal.setSignalType(s.getTypeLocalised());
            signal.setSystemAddress(event.getSystemAddress());
            result.add(signal);
        }
        return result;
    }
}
