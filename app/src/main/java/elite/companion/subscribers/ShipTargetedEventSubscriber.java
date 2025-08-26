package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.ShipTargetedEvent;
import elite.companion.util.RomanNumeralConverter;

public class ShipTargetedEventSubscriber {

    public ShipTargetedEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onShipTargetedEvent(ShipTargetedEvent event) {
        if (!event.isTargetLocked()) {
            VoiceGenerator.getInstance().speak("Contact Lost");
        }

        String localizedShipName = event.getShipLocalised();
        String ship = localizedShipName == null ? "" : RomanNumeralConverter.convertRomanInName(localizedShipName);
        String pilotName = event.getPilotNameLocalised();
        String pilotRank = event.getPilotRank();
        String legalStatus = event.getLegalStatus();
        String pledgedPower = event.getPledgePower();
        String faction = event.getFaction();


        float shieldHealth = event.getShieldHealth();
        float hullHealth = event.getHullHealth();
        StringBuilder info = new StringBuilder();

        if (event.getScanStage() == 0) {
            VoiceGenerator.getInstance().speak("New Contact...");
            return;

        } else if (event.getScanStage() == 1) {
            VoiceGenerator.getInstance().speak("Scanning...");
            return;

        } else if (event.getScanStage() == 2) {
            info.append("Identified: ");
            info.append(ship == null ? " Unknown Ship " : ship);
            info.append(", ");
            info.append(pilotName == null ? " Pilot Unknown " : pilotName.replace("_", " "));
            info.append(", ");
            info.append(pilotRank == null ? " Rank Unknown " : pilotRank.replace("_", " "));
            info.append(", ");
            info.append(legalStatus == null ? " Legal Status Unknown " : legalStatus.replace("_", " "));
            VoiceGenerator.getInstance().speak(info.toString());
            return;

        } else if (event.getScanStage() == 3) {
            info.append("Identified: ");

            info.append(ship == null ? " Unknown Ship " : ship);
            info.append(", ");

            info.append(legalStatus == null ? " Legal Status Unknown " : legalStatus.replace("_", " "));
            info.append(", ");

            info.append(pilotRank == null ? " Rank Unknown " : pilotRank.replace("_", " "));
            info.append(", ");

            info.append(pilotName == null ? " Pilot Unknown " : pilotName.replace("_", " "));
            info.append(", ");

            if (faction != null) info.append("Faction: ").append(faction.replace("_", " "));
            //if(pledgedPower != null) info.append(", ").append("Pledged Power: ").append(pledgedPower).append(", ");

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
            return;
        }
    }
}
