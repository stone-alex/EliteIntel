package elite.intel.util.yaml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * Simple factory providing a pre-configured Jackson ObjectMapper for YAML.
 * Produces clean, block-style YAML that is token-efficient for LLM prompts.
 */
public final class YamlFactory {

    private static final ObjectMapper MAPPER = createMapper();

    private static ObjectMapper createMapper() {
        YAMLFactory factory = new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .disable(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS);

        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.registerModule(new JavaTimeModule());

        // ensure string format, not epoch millis
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // skip all empty lists and fields to save tokens.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * Converts an object (record, POJO, collection, etc.) to a compact YAML string.
     *
     * @param object The object to serialize
     * @return YAML string (block style, no unnecessary quotes)
     * @throws RuntimeException on serialization failure
     */
    public static String toYaml(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to YAML", e);
        }
    }

    private YamlFactory() {
    }
}