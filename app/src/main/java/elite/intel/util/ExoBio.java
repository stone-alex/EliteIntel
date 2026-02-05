package elite.intel.util;

import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;

public class ExoBio {

    public static List<DataDto> completedScansForPlanet(List<BioSampleDto> allBioSamples, String planetName) {
        ArrayList<DataDto> result = new ArrayList<>();
        for(BioSampleDto bioSample : allBioSamples) {
            if(bioSample.getPlanetName().equalsIgnoreCase(planetName)) {
                result.add(new DataDto(bioSample.getGenus(), bioSample.getSpecies(), bioSample.getScanXof3()));
            }
        }

        return result;
    }

    public record DataDto(String genus, String species, Integer scanXof3) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
