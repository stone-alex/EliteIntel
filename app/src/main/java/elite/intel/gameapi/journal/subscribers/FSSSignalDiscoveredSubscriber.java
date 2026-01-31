package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSSignalDiscoveredEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.eddn.EdDnClient;
import elite.intel.search.eddn.ZMQUtil;
import elite.intel.search.eddn.mappers.ScanEventJournalMapper;
import elite.intel.search.eddn.mappers.ScanFssSignalDiscoveredMapper;
import elite.intel.search.eddn.schemas.EddnHeader;
import elite.intel.search.eddn.schemas.EddnPayload;
import elite.intel.search.eddn.schemas.ScanEventJournalMessage;
import elite.intel.search.eddn.schemas.ScanFssSignalDiscoveredMessage;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.session.SystemSession;
import elite.intel.util.TimeUtils;

@SuppressWarnings("unused")
public class FSSSignalDiscoveredSubscriber {

    private static final String USS_TYPE_SALVAGE = "$USS_Type_Salvage";
    private static final String USS_TYPE_VALUABLE_SALVAGE = "$USS_Type_ValuableSalvage";
    private static final String USS_TYPE_VERY_VALUABLE_SALVAGE = "$USS_Type_VeryValuableSalvage";

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final PirateMissionDataManager pirateMissionDataManager = PirateMissionDataManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();
    private final EdDnClient edDnClient = EdDnClient.getInstance();

    @Subscribe
    public void onFSSSignalDiscovered(FSSSignalDiscoveredEvent event) {

        if(systemSession.isExplorationData()) {
            ScanFssSignalDiscoveredMessage msg = ScanFssSignalDiscoveredMapper.map(
                    event,
                    playerSession.getPrimaryStarName(),
                    locationManager.getGalacticCoordinates()
            );

            EddnHeader header = new EddnHeader(ZMQUtil.generateUploaderID());
            header.setGameVersion(playerSession.getGameVersion());
            header.setGameBuild(playerSession.getGameBuild());
            header.setSoftwareVersion(systemSession.readVersionFromResources());

            EddnPayload<ScanFssSignalDiscoveredMessage> payload = new EddnPayload<>(
                    "https://eddn.edcd.io/schemas/journal/1",
                    header,
                    msg
            );
            edDnClient.upload(payload);
        }


        locationManager.save(updateLocation(event));

        if ("ResourceExtraction".equals(event.getSignalType())) {
            pirateMissionDataManager.confirmTargetReconResourceSite(playerSession.getPrimaryStarName());
        }

        if (event.getUssTypeLocalised() != null && event.getUssTypeLocalised().equals("Nonhuman signal source")) {
            publishVoice("Nonhuman signal source detected! Threat level " + event.getThreatLevel() + "!");
        }
        if (event.getUssType() != null && event.getUssType().contains(USS_TYPE_SALVAGE)) {
            announceSalvage("Low quality salvage", event);
        }
        if (event.getUssType() != null && event.getUssType().contains(USS_TYPE_VALUABLE_SALVAGE)) {
            announceSalvage("Valuable salvage", event);
        }
        if (event.getUssType() != null && event.getUssType().contains(USS_TYPE_VERY_VALUABLE_SALVAGE)) {
            announceSalvage("Very Valuable salvage", event);
        }
    }

    private LocationDto updateLocation(FSSSignalDiscoveredEvent event) {
        LocationDto location = locationManager.findBySystemAddress(event.getSystemAddress());
        FssSignalDto signal = new FssSignalDto();
        signal.setSignalName(event.getSignalName());
        signal.setSignalNameLocalised(event.getSignalNameLocalised());
        signal.setSignalType(event.getSignalType());
        signal.setSpawningFaction(event.getSpawningFactionLocalised());
        signal.setSpawningState(event.getSpawningStateLocalised());
        signal.setThreatLevel(event.getThreatLevel());
        signal.setTimeRemaining(event.getTimeRemaining());
        signal.setUssType(event.getUssType());
        signal.setUssTypeLocalised(event.getUssTypeLocalised());
        signal.setSystemAddress(event.getSystemAddress());
        location.addDetectedSignal(signal);
        return location;
    }

    private void publishVoice(String message) {
        EventBusManager.publish(new DiscoveryAnnouncementEvent(message));
    }


    private void announceSalvage(String qualityLabel, FSSSignalDiscoveredEvent event) {
        StringBuilder msg = new StringBuilder()
                .append(qualityLabel).append(" ")
                .append(event.getUssTypeLocalised()).append(": ")
                .append(TimeUtils.secondsToMinutesRemainingString(event.getTimeRemaining()));

        if (event.getThreatLevel() > 0) {
            msg.append(", threat level: ").append(event.getThreatLevel());
        }

        publishVoice(msg.toString());
    }

}