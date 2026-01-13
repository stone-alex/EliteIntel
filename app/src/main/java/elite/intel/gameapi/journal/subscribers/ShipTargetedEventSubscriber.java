package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.ShipTargetedEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.Md5Utils;
import elite.intel.util.RomanNumeralConverter;
import elite.intel.util.TTSFriendlyNumberConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class ShipTargetedEventSubscriber {

    private final Logger log = LogManager.getLogger(ShipTargetedEventSubscriber.class);
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();

    @Subscribe public void onShipTargetedEvent(ShipTargetedEvent event) {

        log.debug(event.toJson());

        if (!event.isTargetLocked()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Contact Lost"));
        }

        String localizedShipName = event.getShipLocalised();
        String ship = localizedShipName == null ? "" : RomanNumeralConverter.convertRomanInName(localizedShipName);
        String pilotName = event.getPilotNameLocalised();
        String pilotRank = event.getPilotRank();
        String legalStatus = event.getLegalStatus() == null ? null : event.getLegalStatus().toLowerCase();
        int bounty = event.getBounty();
        String missionTargetOrNull = isMissionTargetOrNull(event);

        float shieldHealth = event.getShieldHealth();
        float hullHealth = event.getHullHealth();
        StringBuilder info = new StringBuilder();
        if (announceScan(event, legalStatus, missionTargetOrNull)) {

            info.append("Contact: ");
            info.append(missionTargetOrNull);

            info.append(ship == null ? " Unknown Ship " : ship);
            info.append(", ");

            info.append(pilotName == null ? " Pilot Unknown " : pilotName.replace("_", " "));
            info.append(", ");

            info.append(pilotRank == null ? " Rank Unknown " : pilotRank.replace("_", " "));
            info.append(", ");

            info.append(legalStatus == null ? " Legal Status Unknown " : legalStatus.replace("_", " "));
            info.append(", ");

            info.append(bounty == 0 ? "No Bounty" : "bounty: " + TTSFriendlyNumberConverter.formatBountyForSpeech(bounty));
            info.append(", ");

            if (shieldHealth == 100 && hullHealth == 100) {
                //info.append("All Systems Normal");
            } else {
                if (shieldHealth == 0) {
                    info.append("Shields off line");
                } else if (shieldHealth < 50) {
                    info.append(", ");
                    info.append("Shields: ");
                    info.append(String.format("%.0f", shieldHealth)).append(" percent");
                }

                info.append(", ");
                if (hullHealth < 50) {
                    info.append(", ");
                    info.append("Hull: ");
                    info.append(String.format("%.0f", hullHealth)).append(" percent");
                }
            }
            String data = buildCanonicalShipString(event);
            String key = Md5Utils.generateMd5(data);
            if (playerSession.getShipScan(key) == null || playerSession.getShipScan(key).isEmpty()) {
                //new scan
                playerSession.putShipScan(key, data);
                EventBusManager.publish(new TTSInterruptEvent());
                EventBusManager.publish(new MissionCriticalAnnouncementEvent(info.toString()));
            }
        }
    }


    private String buildCanonicalShipString(ShipTargetedEvent event) {
        String pilot = event.getPilotNameLocalised();
        String shipType = event.getShipLocalised();
        String faction = event.getFaction();
        String legalStatus = event.getLegalStatus();
        return pilot + "|" + shipType + "|" + faction + "|" + legalStatus;
    }

    private String isMissionTargetOrNull(ShipTargetedEvent event) {
        String faction = event.getFaction();
        String legalStatus = event.getLegalStatus();
        if (faction == null || faction.isBlank()) return null;
        if (legalStatus == null || legalStatus.isBlank()) return null;

        Set<String> targetFactions = missionManager.getTargetFactions(missionManager.getPirateMissionTypes());
        if (!targetFactions.isEmpty() && targetFactions.contains(faction)) {
            return " Mission Target ";
        }

        if (legalStatus.equalsIgnoreCase("wanted")) {
            return " Legal Target ";
        } else return null;
    }

    private boolean announceScan(ShipTargetedEvent event, String legalStatus, String missionTarget) {
        if (missionTarget == null || missionTarget.isBlank()) return false;
        if (legalStatus == null) return false;
        if (event == null) return false;
        if (legalStatus.isBlank()) return false;
        if ("clean".contains(legalStatus.toLowerCase())) return false;
        if (event.getScanStage() == 0) return false;
        return "wanted".contains(legalStatus.toLowerCase());
    }
}
