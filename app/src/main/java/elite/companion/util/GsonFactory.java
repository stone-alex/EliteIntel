package elite.companion.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Since Java 17 we have to use a custom Gson instance to handle the Instant type.
 * if we use new Gson() as is, we will have errors parsing Instant values everywhere.
 * */
public final class GsonFactory {
    private static final Gson GSON = new GsonBuilder()
            .setLenient() // Keep lenient parsing for journal JSON
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
            .create();

    public static Gson getGson() {
        return GSON;
    }

    // Prevent instantiation
    private GsonFactory() {}
}