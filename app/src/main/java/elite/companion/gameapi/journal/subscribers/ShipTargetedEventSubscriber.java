package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.ShipTargetedEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;
import elite.companion.util.RomanNumeralConverter;
import elite.companion.util.TTSFriendlyNumberConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShipTargetedEventSubscriber {

    private  final Logger log = LoggerFactory.getLogger(ShipTargetedEventSubscriber.class);

    @Subscribe
    public void onShipTargetedEvent(ShipTargetedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        log.info(event.toJson());

        if (!event.isTargetLocked()) {
            EventBusManager.publish(new VoiceProcessEvent("Contact Lost"));
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
        if (anounceScan(event, legalStatus, missionTarget)) {

            info.append("Contact: ");
            info.append(missionTarget);

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
                if(shieldHealth == 0) {
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
            String key;
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(data.getBytes(StandardCharsets.UTF_8));
                byte[] digest = md5.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                key = sb.toString();
                if (playerSession.getShipScan(key) == null || playerSession.getShipScan(key).isEmpty()) {
                    //new scan
                    playerSession.putShipScan(key, data);
                    EventBusManager.publish(new VoiceProcessEvent(info.toString()));
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private String buildCanonicalShipString(ShipTargetedEvent event) {  // Cast to actual type, e.g., ScanEvent
        // Example assuming info is a ScanEvent POJO with getters
        String pilot = event.getPilotNameLocalised();
        String shipType = event.getShipLocalised();
        String faction = event.getFaction();
        String legalStatus = event.getLegalStatus();
        // Concat with delimiters to avoid ambiguity (e.g., "JohnDoe|Anaconda|Empire|Clean")
        return pilot + "|" + shipType + "|" + faction + "|" + legalStatus;
    }

    private String determineMissionTarget(ShipTargetedEvent event) {
        String faction = event.getFaction();
        String legalStatus = event.getLegalStatus();
        if (faction == null || faction.isBlank()) return "";
        if (legalStatus == null || legalStatus.isBlank()) return "";
        PlayerSession playerSession = PlayerSession.getInstance();
        String targetFaction = String.valueOf(playerSession.get(PlayerSession.TARGET_FACTION_NAME));
        if (targetFaction.equalsIgnoreCase(faction) && legalStatus.equalsIgnoreCase("wanted")) {
            return "Mission Target! ";
        } else return "";
    }

    private  boolean anounceScan(ShipTargetedEvent event, String legalStatus, String missionTarget) {
        if (missionTarget == null || missionTarget.isBlank()) return false;
        if (legalStatus == null) return false;
        if (event == null) return false;
        if (legalStatus.isBlank()) return false;
        if ("wanted".contains(legalStatus.toLowerCase())) return true;
        if ("clean".contains(legalStatus.toLowerCase())) return false;
        if (event.getScanStage() == 0) return false;

        return true;
    }

}
