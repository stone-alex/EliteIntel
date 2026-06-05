package elite.intel.ai.brain.actions.macro;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MacroExecutionContextTest {

    // --- resolveString ---

    @Test
    void resolveStringReplacesKnownParam() {
        MacroExecutionContext ctx = contextWithJson("{\"speed\": \"50\"}");
        assertEquals("set speed 50", ctx.resolveString("set speed ${speed}"));
    }

    @Test
    void resolveStringMultipleParams() {
        MacroExecutionContext ctx = contextWithJson("{\"lat\": \"-10.5\", \"lon\": \"45.2\"}");
        assertEquals("navigate to -10.5, 45.2", ctx.resolveString("navigate to ${lat}, ${lon}"));
    }

    @Test
    void resolveStringNullTemplateReturnsNull() {
        MacroExecutionContext ctx = contextWithJson("{}");
        assertNull(ctx.resolveString(null));
    }

    @Test
    void resolveStringNoRefsReturnedUnchanged() {
        MacroExecutionContext ctx = contextWithJson("{}");
        assertEquals("hello pilot", ctx.resolveString("hello pilot"));
    }

    @Test
    void resolveStringThrowsForUnresolvedRef() {
        MacroExecutionContext ctx = contextWithJson("{}");
        assertThrows(UnresolvedMacroParamException.class,
                () -> ctx.resolveString("navigate to ${lat}"));
    }

    // --- resolveStepParams ---

    @Test
    void resolveStepParamsPreservesNumberTypeForBareRef() {
        MacroExecutionContext ctx = contextWithJson("{\"lat\": -10.5, \"lon\": 45.2}");
        JsonObject result = ctx.resolveStepParams(Map.of("lat", "${lat}", "lon", "${lon}"));
        // Bare refs must preserve JSON number type, not coerce to string.
        assertFalse(result.get("lat").isJsonPrimitive() && result.get("lat").getAsJsonPrimitive().isString());
        assertEquals(-10.5, result.get("lat").getAsDouble(), 0.001);
        assertEquals(45.2, result.get("lon").getAsDouble(), 0.001);
    }

    @Test
    void resolveStepParamsPreservesBooleanTypeForBareRef() {
        MacroExecutionContext ctx = contextWithJson("{\"enabled\": true}");
        JsonObject result = ctx.resolveStepParams(Map.of("state", "${enabled}"));
        assertTrue(result.get("state").getAsBoolean());
        assertTrue(result.get("state").getAsJsonPrimitive().isBoolean());
    }

    @Test
    void resolveStepParamsMixedTemplateBecomesString() {
        MacroExecutionContext ctx = contextWithJson("{\"name\": \"gold\"}");
        JsonObject result = ctx.resolveStepParams(Map.of("key", "find_${name}"));
        assertEquals("find_gold", result.get("key").getAsString());
    }

    @Test
    void resolveStepParamsEmptyMappingReturnsEmptyObject() {
        MacroExecutionContext ctx = contextWithJson("{\"lat\": 1.0}");
        JsonObject result = ctx.resolveStepParams(Map.of());
        assertTrue(result.entrySet().isEmpty());
    }

    @Test
    void resolveStepParamsNullMappingReturnsEmptyObject() {
        MacroExecutionContext ctx = contextWithJson("{}");
        JsonObject result = ctx.resolveStepParams(null);
        assertTrue(result.entrySet().isEmpty());
    }

    @Test
    void resolveStepParamsThrowsForUnresolvedBareRef() {
        MacroExecutionContext ctx = contextWithJson("{}");
        assertThrows(UnresolvedMacroParamException.class,
                () -> ctx.resolveStepParams(Map.of("lat", "${lat}")));
    }

    // --- validateRequiredParams ---

    @Test
    void validateRequiredParamsNoErrorsWhenAllPresent() {
        MacroDefinition macro = macroWithParams(List.of(
                new MacroParameterSpec("lat", "number", true, "", null, null),
                new MacroParameterSpec("lon", "number", true, "", null, null)
        ));
        MacroExecutionContext ctx = MacroExecutionContext.fromJson(macro,
                JsonParser.parseString("{\"lat\": -10.5, \"lon\": 45.2}").getAsJsonObject());
        assertTrue(ctx.validateRequiredParams().isEmpty());
    }

    @Test
    void validateRequiredParamsReportsEachMissingRequired() {
        MacroDefinition macro = macroWithParams(List.of(
                new MacroParameterSpec("lat", "number", true, "", null, null),
                new MacroParameterSpec("lon", "number", true, "", null, null),
                new MacroParameterSpec("comment", "string", false, "", null, null)
        ));
        MacroExecutionContext ctx = MacroExecutionContext.fromJson(macro, new JsonObject());
        List<String> errors = ctx.validateRequiredParams();
        assertEquals(2, errors.size(), "Only required params should be reported missing");
        assertTrue(errors.stream().anyMatch(e -> e.contains("lat")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("lon")));
    }

    @Test
    void validateRequiredParamsIgnoresOptional() {
        MacroDefinition macro = macroWithParams(List.of(
                new MacroParameterSpec("hint", "string", false, "", null, null)
        ));
        MacroExecutionContext ctx = MacroExecutionContext.fromJson(macro, new JsonObject());
        assertTrue(ctx.validateRequiredParams().isEmpty());
    }

    @Test
    void validateRequiredParamsNoErrorsForParamlessMacro() {
        MacroDefinition macro = macroWithParams(List.of());
        MacroExecutionContext ctx = MacroExecutionContext.fromJson(macro, new JsonObject());
        assertTrue(ctx.validateRequiredParams().isEmpty());
    }

    // --- helpers ---

    private static MacroExecutionContext contextWithJson(String json) {
        MacroDefinition macro = macroWithParams(List.of());
        JsonObject params = JsonParser.parseString(json).getAsJsonObject();
        return MacroExecutionContext.fromJson(macro, params);
    }

    private static MacroDefinition macroWithParams(List<MacroParameterSpec> params) {
        return new MacroDefinition("test", "Test", "", "test phrase", params,
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
    }
}
