package elite.companion.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;

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