package elite.companion.util.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * A custom Gson type adapter for serializing and deserializing {@link Instant} objects.
 * This adapter handles the parsing and formatting of {@link Instant}s in ISO-8601 format.
 * <p>
 * This class is primarily used to ensure compatibility with Gson when working with
 * {@link Instant}, which is not natively supported by default Gson adapters.
 * <p>
 * The {@link Instant} serialization produces a {@link JsonPrimitive} containing the
 * ISO-8601 string representation of the instant (e.g., "2023-10-05T14:48:00Z").
 * During deserialization, the adapter expects an ISO-8601 formatted string and converts
 * it back to an {@link Instant}.
 * <p>
 * Usage:
 * - Registers this adapter in a {@link GsonBuilder} using its `registerTypeAdapter` method.
 * - Can be utilized wherever Gson is used for processing JSON objects containing {@link Instant} values.
 * <p>
 * Throws:
 * - {@link JsonParseException} when deserialization fails due to parsing errors.
 */
public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String timestamp = json.getAsString();
        try {
            return Instant.parse(timestamp); // Handles Elite Dangerous journal timestamps (ISO-8601)
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Failed to parse Instant from: " + timestamp, e);
        }
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}