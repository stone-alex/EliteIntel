package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.*;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.util.StringUtls.subtractString;

public class SAASignalsFoundSubscriber {

    @Subscribe
    public void onSAASignalsFound(SAASignalsFoundEvent event) {
        StringBuilder sb = new StringBuilder();
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (!currentLocation.getPlanetName().equalsIgnoreCase(event.getBodyName())) {
            StellarObjectDto dto = playerSession.getStellarObject(event.getBodyName());
            if (dto.getName().equalsIgnoreCase(event.getBodyName())) {
                currentLocation.setPlanetName(dto.getName());
                currentLocation.setGravity(dto.getSurfaceGravity());
                currentLocation.setOurDiscovery(dto.isOurDiscovery());
                currentLocation.setSurfaceTemperature(dto.getSurfaceTemperature());
                currentLocation.setPlanetShortName(subtractString(dto.getName(), currentLocation.getStarName()));
            }
        }

        currentLocation.addSaaSignals(event.getSignals());
        playerSession.saveCurrentLocation(currentLocation);

        List<SAASignalsFoundEvent.Signal> signals = event.getSignals();
        int signalsFound = signals != null ? signals.size() : 0;

        String bodyName = event.getBodyName();
        StellarObjectDto stellarObjectDto = playerSession.getStellarObject(bodyName);
        stellarObjectDto.setOurDiscovery(playerSession.getCurrentLocation().isOurDiscovery());


        if (signalsFound > 0) {
            int liveSignals = event.getGenuses() != null ? event.getGenuses().size() : 0;
            sb.append(" Signal(s) found: ");
            for (SAASignalsFoundEvent.Signal signal : signals) {
                sb.append(" Type: ").append(signal.getType()).append(". ");
                if ("Tritium".equals(signal.getType())) {
                    sb.append(" Carrier fuel source is detected. ");
                }
            }


            if (liveSignals > 0) {
                stellarObjectDto.setNumberOfBioFormsPresent(liveSignals);
                stellarObjectDto.setGenus(toGenusDto(event.getGenuses()));
                stellarObjectDto.setBioFormsPresent(true);
                playerSession.addStellarObject(stellarObjectDto);
                currentLocation.setGenus(toGenusDto(event.getGenuses()));
                playerSession.saveCurrentLocation(currentLocation);
                currentLocation.setBioFormsPresent(true);

                boolean hasBeenScanned = scanBioCompleted(event, playerSession);

                if (!hasBeenScanned) sb.append(" Exobiology signal(s) found ").append(liveSignals).append(": ");
                long averageProjectedPayment = 0;
                for (SAASignalsFoundEvent.Genus genus : event.getGenuses()) {
                    averageProjectedPayment = averageProjectedPayment + BioForms.getAverageProjectedPayment(genus.getGenusLocalised());
                    sb.append(" ");
                    sb.append(genus.getGenusLocalised());
                    sb.append(", ");
                }
                if (!hasBeenScanned) sb.append("Average projected payment: ").append(averageProjectedPayment).append(" credits. Plus bonus if first discovered.");

            } else if (bodyName.contains("Ring")) {
                //Rings are bodies
                StellarObjectDto ring = new StellarObjectDto();
                ring.setBodyId(event.getBodyID());
                ring.setName(bodyName);
                ring.setMaterials(toMaterials(event.getSignals()));

                String parentBodyName = bodyName.substring(0, bodyName.length() - " X Ring".length());
                StellarObjectDto parent = playerSession.getStellarObject(parentBodyName);
                parent.setHasRings(true);
                if(event.getSignals() != null) {
                    ring.setSaasSignals(event.getSignals());
                    ring.setGeoSignals(event.getSignals().size());
                    parent.setSaasSignals(event.getSignals());
                }
                playerSession.addStellarObject(ring);
                playerSession.addStellarObject(parent);
            }

            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        } else {
            EventBusManager.publish(new SensorDataEvent("No Signal(s) detected."));
        }
    }

    private boolean scanBioCompleted(SAASignalsFoundEvent event, PlayerSession playerSession) {
        List<BioSampleDto> bioSamples = playerSession.getBioCompletedSamples();
        for (SAASignalsFoundEvent.Genus genus : event.getGenuses()) {
            for (BioSampleDto bioSampleDto : bioSamples) {
                boolean matchingGenus = bioSampleDto.getGenus().equalsIgnoreCase(genus.getGenusLocalised());
                boolean samePlanet = bioSampleDto.getPlanetName().equalsIgnoreCase(event.getBodyName());
                if (matchingGenus && samePlanet) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<MaterialDto> toMaterials(List<SAASignalsFoundEvent.Signal> signals) {
        ArrayList<MaterialDto> materialDtos = new ArrayList<>();
        for (SAASignalsFoundEvent.Signal signal : signals) {
            materialDtos.add(new MaterialDto(signal.getType(), 100, true));
        }
        return materialDtos;
    }

    private List<GenusDto> toGenusDto(List<SAASignalsFoundEvent.Genus> genuses) {
        ArrayList<GenusDto> result = new ArrayList<>();
        for(SAASignalsFoundEvent.Genus genus: genuses) {
            GenusDto dto = new GenusDto();
            dto.setSpecies(genus.getGenusLocalised());
            dto.setRewardInCredits(BioForms.getAverageProjectedPayment(genus.getGenusLocalised()));
            result.add(dto);
        }
        return result;
    }
}
