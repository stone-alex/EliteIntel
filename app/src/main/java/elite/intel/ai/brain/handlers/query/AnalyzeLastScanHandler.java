package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class AnalyzeLastScanHandler  extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        StellarObjectDto lastScan = playerSession.getLastScan();
        List<SAASignalsFoundEvent.Signal> saaSignals = playerSession.getCurrentLocation().getSaaSignals();

        return analyzeData(new DataDto(lastScan, saaSignals).toJson(), originalUserInput);
    }

    record DataDto(StellarObjectDto lastScan, List<SAASignalsFoundEvent.Signal> saaSignals) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
