package elite.intel.search.spansh.station.vista;

import com.google.gson.JsonObject;
import elite.intel.search.spansh.station.StationSearchClient;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class VistaGenomicsSearch {

    public static final String VISTA_GENOMICS = "Vista Genomics";
    private static final Logger log = LogManager.getLogger(VistaGenomicsSearch.class);

    public static List<VistaGenomicsLocationDto.Result> findVistaGenomics(ToJsonConvertible searchCriteria) {
        try {
            StationSearchClient client = StationSearchClient.getInstance();
            VistaGenomicsLocationDto dto = client.searchVistaGenomics(searchCriteria);

            return dto.getResults().stream().filter(
                    result -> result.getServices().stream().anyMatch(service -> VISTA_GENOMICS.equalsIgnoreCase(service.getName())
                    )
            ).toList();

        } catch (Exception e) {
            log.error("Failed to find material trader", e);
        }
        return null;
    }
}
