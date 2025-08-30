package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.journal.events.ShipTargetedEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.RomanNumeralConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipTargetedEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(ShipTargetedEventSubscriber.class);

    @Subscribe
    public void onShipTargetedEvent(ShipTargetedEvent event) {

        log.info(event.toJson());

        if (!event.isTargetLocked()) {
            VoiceGenerator.getInstance().speak("Contact Lost");
        }

        String localizedShipName = event.getShipLocalised();
        String ship = localizedShipName == null ? "" : RomanNumeralConverter.convertRomanInName(localizedShipName);
        String pilotName = event.getPilotNameLocalised();
        String pilotRank = event.getPilotRank();
        String legalStatus = event.getLegalStatus() == null ? null : event.getLegalStatus().toLowerCase();
        int bounty = event.getBounty();
        String missionTarget = determineMissionTarget(event);

        float shieldHealth = event.getShieldHealth();
        float hullHealth = event.getHullHealth();
        StringBuilder info = new StringBuilder();
        if (anounceScan(event, legalStatus)) {

            info.append("Contact Identified: ");
            info.append(missionTarget);

            info.append(ship == null ? " Unknown Ship " : ship);
            info.append(", ");

            info.append(pilotName == null ? " Pilot Unknown " : pilotName.replace("_", " "));
            info.append(", ");

            info.append(pilotRank == null ? " Rank Unknown " : pilotRank.replace("_", " "));
            info.append(", ");

            info.append(legalStatus == null ? " Legal Status Unknown " : legalStatus.replace("_", " "));
            info.append(", ");

            info.append(bounty == 0 ? "No Bounty" : "bounty: " + bounty + " credits");
            info.append(", ");

            if (shieldHealth == 100 && hullHealth == 100) {
                //info.append("All Systems Normal");
            } else {
                if (shieldHealth > 0) {
                    info.append(", ");
                    info.append("Shields: ");
                    info.append(String.format("%.0f", shieldHealth)).append(" percent");
                } else {
                    info.append("shields off line");
                }
                info.append(", ");
                if (hullHealth > 0) {
                    info.append(", ");
                    info.append("Hull: ");
                    info.append(String.format("%.0f", hullHealth)).append(" percent");
                }
            }

            VoiceGenerator.getInstance().speak(info.toString());
        }
    }

    private String determineMissionTarget(ShipTargetedEvent event) {
        String faction = event.getFaction();
        String legalStatus = event.getLegalStatus();
        if (faction == null || faction.isBlank()) return "";
        if (legalStatus == null || legalStatus.isBlank()) return "";
        SystemSession systemSession = SystemSession.getInstance();
        String targetFaction = String.valueOf(systemSession.get(SystemSession.TARGET_FACTION_NAME));
        if (targetFaction.equalsIgnoreCase(faction) && legalStatus.equalsIgnoreCase("wanted")) {
            return "Mission Target! ";
        } else return "";
    }

    private static boolean anounceScan(ShipTargetedEvent event, String legalStatus) {
        if (legalStatus == null) return false;
        if (event == null) return false;
        if (legalStatus.isBlank()) return false;
        if ("wanted".contains(legalStatus.toLowerCase())) return true;
        if ("clean".contains(legalStatus.toLowerCase())) return false;
        if (event.getScanStage() == 0) return false;

        return true;
    }
}
