package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.ShipTargetedEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.RomanNumeralConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipTargetedEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(ShipTargetedEventSubscriber.class);

    public ShipTargetedEventSubscriber() {
        EventBusManager.register(this);
    }

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
        String pledgedPower = event.getPledgePower();
        String faction = event.getFaction();
        int bounty = event.getBounty();


        float shieldHealth = event.getShieldHealth();
        float hullHealth = event.getHullHealth();
        StringBuilder info = new StringBuilder();
        if (anounceScan(event, legalStatus)) {

            info.append("Contact Identified: ");

            info.append(ship == null ? " Unknown Ship " : ship);
            info.append(", ");

            info.append(pilotName == null ? " Pilot Unknown " : pilotName.replace("_", " "));
            info.append(", ");

            info.append(pilotRank == null ? " Rank Unknown " : pilotRank.replace("_", " "));
            info.append(", ");

            info.append(legalStatus == null ? " Legal Status Unknown " : legalStatus.replace("_", " "));
            info.append(", ");

            String bountyString = bounty == 0 ? "No Bounty" : bounty + " credits";
            if (bounty > 0) {
                SystemSession.getInstance().setSensorData("Targeted ship has bounty of: '"+bountyString+"'");
            }

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
