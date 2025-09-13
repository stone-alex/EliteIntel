package elite.intel.util.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * A custom type adapter for handling serialization and deserialization of {@link ZonedDateTime}
 * objects in JSON using Gson.
 * <p>
 * This adapter is used to ensure proper conversion of ZonedDateTime objects to and from their
 * ISO-8601 string representation during JSON parsing and generation. It can handle potential
 * parsing exceptions gracefully by throwing a {@link JsonParseException}.
 * <p>
 * The {@code serialize} method is responsible for converting a {@link ZonedDateTime}
 * instance into its string representation for inclusion in JSON.
 * <p>
 * The {@code deserialize} method is responsible for parsing a JSON string representation
 * into a {@link ZonedDateTime} instance.
 * <p>
 * Implements:
 * - {@link JsonSerializer} for custom serialization logic.
 * - {@link JsonDeserializer} for custom deserialization logic.
 * <p>
 * Usage scenarios include registering this adapter in a {@link GsonBuilder}
 * for automatic handling of {@link ZonedDateTime}-based fields when working with Gson instances.
 */
public class ZonedDateTimeTypeAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
    @Override
    public ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String timestamp = json.getAsString();
        try {
            return ZonedDateTime.parse(timestamp);
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Failed to parse ZonedDateTime from: " + timestamp, e);
        }
    }

    @Override
    public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}