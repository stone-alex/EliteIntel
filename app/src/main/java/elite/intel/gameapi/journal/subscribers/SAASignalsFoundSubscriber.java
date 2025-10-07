package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.PLANETARY_RING;

public class SAASignalsFoundSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onSAASignalsFound(SAASignalsFoundEvent event) {
        StringBuilder sb = new StringBuilder();

        LocationDto location = playerSession.getLocation(event.getBodyID());
        location.addSaaSignals(event.getSignals());
        playerSession.saveLocation(location);

        List<SAASignalsFoundEvent.Signal> signals = event.getSignals();
        int signalsFound = signals != null ? signals.size() : 0;

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
                if(location.getBioSignals() < liveSignals) {
                    location.setBioSignals(liveSignals);
                }
                location.setGenus(toGenusDto(event.getGenuses()));
                boolean hasBeenScanned = scanBioCompleted(event, playerSession);

                if (!hasBeenScanned) sb.append(" Exobiology signal(s) found ").append(liveSignals).append(": ");

                long averageProjectedPayment = 0;
                long averageFirstDiscoveryBonus = 0;
                for (SAASignalsFoundEvent.Genus genus : event.getGenuses()) {
                    BioForms.ProjectedPayment averagePayment = BioForms.getAverageProjectedPayment(genus.getGenusLocalised());
                    if (averagePayment != null) {
                        averageProjectedPayment = averagePayment.payment();
                        averageFirstDiscoveryBonus =averagePayment.firstDiscoveryBonus();
                    }

                    if (!hasBeenScanned) {
                        sb.append(" ");
                        sb.append(genus.getGenusLocalised());
                        sb.append(", ");
                    }
                }
                if (!hasBeenScanned) {
                    sb.append("Average projected payment of: ").append(averageProjectedPayment).append(" credits.");
                    if(location.isOurDiscovery()) {
                        sb.append(" Plus average bonus of: ").append(averageFirstDiscoveryBonus).append(" credits for first discovery.");
                    }
                }

            } else if (event.getBodyName().contains("Ring")) {
                //Rings are bodies
                LocationDto ring = new LocationDto(event.getBodyID());
                ring.setLocationType(PLANETARY_RING);
                ring.setBodyId(event.getBodyID());
                ring.setPlanetName(event.getBodyName());
                ring.setMaterials(toMaterials(event.getSignals()));
                ring.setLocationType(PLANETARY_RING);

                String parentBodyName = event.getBodyName().substring(0, event.getBodyName().length() - " X Ring".length());
                LocationDto parent = playerSession.getLocation(findParentId(parentBodyName));
                if(parent != null) parent.setHasRings(true);
                if(event.getSignals() != null) {
                    ring.setSaaSignals(event.getSignals());
                    ring.setGeoSignals(event.getSignals().size());
                    if(parent != null) parent.setSaaSignals(event.getSignals());
                }
                playerSession.saveLocation(ring);
                if(parent != null) playerSession.saveLocation(parent);
            }

            if(playerSession.isDiscoveryAnnouncementOn()) {
                EventBusManager.publish(new SensorDataEvent(sb.toString()));
            }
        } else {
            if(playerSession.isDiscoveryAnnouncementOn()) {
                EventBusManager.publish(new SensorDataEvent("No Signal(s) detected."));
            }
        }

        playerSession.saveLocation(location);
    }

    private long findParentId(String parentBodyName) {
        Collection<LocationDto> values = playerSession.getLocations().values();
        for (LocationDto dto : values) {
            if (dto.getPlanetName().equalsIgnoreCase(parentBodyName)) {
                return dto.getBodyId();
            }
        }
        return 0;
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
            BioForms.ProjectedPayment projectedPayment = BioForms.getAverageProjectedPayment(genus.getGenusLocalised());
            if (projectedPayment != null && projectedPayment.payment() != null) {
                dto.setRewardInCredits(projectedPayment.payment() + projectedPayment.firstDiscoveryBonus());
            }
            result.add(dto);
        }
        return result;
    }
}
