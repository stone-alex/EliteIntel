package elite.intel.ai.brain.handlers.query.struct;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.BaseQueryAnalyzer;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.EncodedMaterialsDto;
import elite.intel.ai.search.edsm.dto.MaterialsDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.EDMaterialCaps;
import elite.intel.session.MaterialsData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.LinkedHashMap;
import java.util.Map;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_MATERIALS_ON_HAND;

public class AnalyseMaterialsHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Accessing materials log data. Stand by."));

        MaterialsData localMaterialsData = MaterialsData.getInstance();
        MaterialsDto rawAndManufacturedMaterials = localMaterialsData.getMaterials();
        EncodedMaterialsDto encodedMaterials = localMaterialsData.getEncodedMaterialsDto();

        if (rawAndManufacturedMaterials == null || rawAndManufacturedMaterials.getMaterials().isEmpty()) {
            rawAndManufacturedMaterials = EdsmApiClient.getMaterials();
        }

        if (encodedMaterials == null || encodedMaterials.getEncoded().isEmpty()) {
            encodedMaterials = EdsmApiClient.getEncodedMaterials();
        }

        Map<String, Integer> encodedMaterialsCap = new LinkedHashMap<>();
        encodedMaterials.getEncoded().forEach(entry -> {
            encodedMaterialsCap.put(entry.getMaterialName(), EDMaterialCaps.getMax(entry.getMaterialName()));
        });

        Map<String, Integer> rawAndManufacturedMaterialsCap = new LinkedHashMap<>();
        rawAndManufacturedMaterials.getMaterials().forEach(entry -> {
            rawAndManufacturedMaterialsCap.put(entry.getMaterialName(), EDMaterialCaps.getMax(entry.getMaterialName()));
        });


        return process(
                new AiDataStruct(ANALYZE_MATERIALS_ON_HAND.getInstructions(),
                        new DataDto(
                                encodedMaterials,
                                rawAndManufacturedMaterials,
                                encodedMaterialsCap,
                                rawAndManufacturedMaterialsCap
                        )
                ),
                originalUserInput
        );
    }

    record DataDto(EncodedMaterialsDto encodedMaterials, MaterialsDto rawAndManufacturedMaterials, Map<String, Integer> encodedMaterialsCap, Map<String, Integer> rawAndManufacturedMaterialsCap) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
