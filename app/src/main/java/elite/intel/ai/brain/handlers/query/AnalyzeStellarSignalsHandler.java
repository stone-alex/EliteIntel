package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.StringUtls;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnalyzeStellarSignalsHandler extends BaseQueryAnalyzer implements QueryHandler {
    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        List<Signal> signals = toSignalsList(playerSession.getLocations());

        String instructions = """
                You are a strict data-only responder for Elite Dangerous mining & exploration signals.
                Use ONLY the information present in the provided JSON array called "signals".
                Never guess, never add fictional bodies, never calculate anything not explicitly in the data.
                User question: "are there any planetary rings with hotspots"
                Answer style rules — follow exactly:
                    - Start with a clear Yes / Non
                    - Then give a structured, detailed summary using natural sentencesn
                    - Group related information (e.g. all hotspots of one ring/body together)
                    - Include the body/ring name, hotspot type, and count for every entry that matches the question
                    - Use clean, readable formatting inside the string (new lines, bullet-style with - or *, short paragraphs)
                    - Be concise but comprehensive — do not skip any matching entries
                    - If nothing matches → write only: "No planetary rings with hotspots found."
                    Good output style examples (reference only – do NOT copy word-for-word):
                    "Yes, several planetary rings in this system contain mineable hotspots:
                    4 A Ringn- Rhodplumsite ×2
                    - Platinum ×2
                    - Monazite ×1
                    - Painite ×1
                    4 B Ring- Opal ×2
                    - Grandidierite ×1
                    - Low Temperature Diamond ×1
                    Output ONLY the requested JSON structure — nothing else.
                """;

        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(
                                signals
                        )
                ),
                originalUserInput
        );
    }

    private List<Signal> toSignalsList(Map<Long, LocationDto> locations) {
        Collection<LocationDto> values = locations.values();
        List<Signal> signals = new ArrayList<>();
        for (LocationDto location : values) {
            List<SAASignalsFoundEvent.Signal> saaSignals = location.getSaaSignals();
            List<FSSBodySignalsEvent.Signal> fssSignals = location.getFssSignals();
            if (saaSignals != null && !saaSignals.isEmpty()) {
                for (SAASignalsFoundEvent.Signal s : saaSignals) {
                    signals.add(new Signal(location.getPlanetShortName(), StringUtls.humanizeBindingName(s.getType()), s.getCount()));
                }
            }
            if (fssSignals != null && !fssSignals.isEmpty()) {
                for (FSSBodySignalsEvent.Signal s : fssSignals) {
                    signals.add(new Signal(location.getPlanetShortName(), s.getTypeLocalised(), s.getCount()));
                }
            }
        }
        return signals;
    }

    record Signal(String planetName, String hotspot, int count) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record DataDto(List<Signal> signals) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
