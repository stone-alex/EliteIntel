package elite.intel.util;

import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;

public class ExoBio {

    public static List<BioSampleDto> completedScansForPlanet(PlayerSession playerSession) {
        List<BioSampleDto> allBioSamples = playerSession.getBioCompletedSamples();
        List<BioSampleDto> completedForThisPlanet = new ArrayList<>();
        for(BioSampleDto bioSample : allBioSamples) {
            if(bioSample.getPlanetName().equalsIgnoreCase(playerSession.getCurrentLocation().getPlanetName())) {
                completedForThisPlanet.add(bioSample);
            }
        }
        return completedForThisPlanet;
    }
}
