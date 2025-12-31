package elite.intel.search.eddn.mappers;

import elite.intel.db.dao.LocationDao;
import elite.intel.gameapi.journal.events.FSSSignalDiscoveredEvent;
import elite.intel.search.eddn.schemas.ScanFssSignalDiscoveredMessage;

import java.util.Arrays;
import java.util.Collections;

public class ScanFssSignalDiscoveredMapper {

    public static ScanFssSignalDiscoveredMessage map(FSSSignalDiscoveredEvent event, String starSystem, LocationDao.Coordinates coordinates) {
        ScanFssSignalDiscoveredMessage msg = new ScanFssSignalDiscoveredMessage();

        msg.setTimestamp(event.getTimestamp());
        msg.setSystemAddress(event.getSystemAddress());

        msg.setStarSystem(starSystem);
        msg.setStarPos(Arrays.asList(coordinates.x(), coordinates.y(), coordinates.z()));

        ScanFssSignalDiscoveredMessage.Signal signal = new ScanFssSignalDiscoveredMessage.Signal();
        signal.setTimestamp(event.getTimestamp());
        signal.setSignalName(event.getSignalName());
        signal.setSignalType(event.getSignalType());

        String ussType = event.getUssType();
        if (ussType != null && !"$USS_Type_MissionTarget;".equals(ussType)) {
            signal.setUssType(ussType);
        }

        signal.setSpawningState(event.getSpawningState());
        signal.setSpawningFaction(event.getSpawningFaction());

        if (event.getThreatLevel() > 0) {
            signal.setThreatLevel(event.getThreatLevel());
        }

        msg.setSignals(Collections.singletonList(signal));

        return msg;
    }
}